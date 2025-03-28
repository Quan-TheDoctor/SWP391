package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.JobApplicationDTO;
import com.se1873.js.springboot.project.entity.JobApplication;
import com.se1873.js.springboot.project.service.JobApplicationService;
import com.se1873.js.springboot.project.service.JobPositionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/resume")
public class ResumeController {
  private final String pythonServiceUrl = "http://localhost:5000";
  private final RestTemplate restTemplate;
  private final JobApplicationService jobApplicationService;
  private final JobPositionService jobPositionService;

  public ResumeController(RestTemplate restTemplate, 
                         JobApplicationService jobApplicationService,
                         JobPositionService jobPositionService) {
    this.restTemplate = restTemplate;
    this.jobApplicationService = jobApplicationService;
    this.jobPositionService = jobPositionService;
  }

  @GetMapping
  public String resume(Model model) {
    log.debug("Loading open positions for resume upload page");
    model.addAttribute("openPositions", jobPositionService.getOpenPositions());
    model.addAttribute("jobApplicationDTO", new JobApplicationDTO());
    model.addAttribute("contentFragment", "fragments/upload-pdf");
    return "index";
  }

  @PostMapping("/parse_resume")
  @ResponseBody
  public ResponseEntity<?> parseResume(@RequestParam("file") MultipartFile file) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("file", new ByteArrayResource(file.getBytes()) {
        @Override
        public String getFilename() {
          return file.getOriginalFilename();
        }
      });

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        pythonServiceUrl + "/parse_resume",
        HttpMethod.POST,
        requestEntity,
        Map.class
      );

      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An error occurred while parsing the resume: " + e.getMessage());
    }
  }

  @GetMapping("/resume_cache/{cacheId}")
  @ResponseBody
  public ResponseEntity<String> getResumeCache(@PathVariable String cacheId) {
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(
        pythonServiceUrl + "/resume_cache/" + cacheId,
        String.class
      );

      return ResponseEntity
        .status(response.getStatusCode())
        .headers(response.getHeaders())
        .body(response.getBody());

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @PostMapping("/save-candidate")
  @ResponseBody
  public ResponseEntity<?> saveCandidate(@Valid @RequestBody JobApplicationDTO jobApplicationDTO,
                                       BindingResult bindingResult) {
    log.info("Received request to save candidate profile: {}", jobApplicationDTO);
    
    if (bindingResult.hasErrors()) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("messageType", "error");
      
      // Xử lý từng lỗi validation cụ thể
      String errorMessage = bindingResult.getFieldErrors().stream()
          .map(error -> {
              switch (error.getField()) {
                  case "jobPositionId":
                      return "Please select a job position";
                  case "candidateName":
                      return "Please enter your full name";
                  case "email":
                      return "Invalid email format";
                  case "phone":
                      return "Phone number must be 10 digits";
                  default:
                      return error.getDefaultMessage();
              }
          })
          .collect(Collectors.joining(", "));
      
      response.put("message", errorMessage);
      return ResponseEntity.badRequest().body(response);
    }

    try {
      JobApplication savedApplication = jobApplicationService.saveJobApplication(jobApplicationDTO);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("messageType", "success");
      response.put("message", "Candidate profile saved successfully!");
      response.put("redirectUrl", "/recruitment/positions/" + jobApplicationDTO.getJobPositionId());
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error saving candidate profile: ", e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("messageType", "error");
      response.put("message", "An error occurred while saving candidate profile: " + e.getMessage());
      
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}