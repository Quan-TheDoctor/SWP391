package com.se1873.js.springboot.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/resume")
public class ResumeController {
  private final String pythonServiceUrl = "http://localhost:5000";
  private final RestTemplate restTemplate;

  public ResumeController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @GetMapping
  public String resume(Model model) {

    model.addAttribute("contentFragment", "fragments/upload-pdf");
    return "index";
  }
  @PostMapping("/parse_resume")
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
}