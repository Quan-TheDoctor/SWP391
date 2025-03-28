package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.dto.JobApplicationDTO;
import com.se1873.js.springboot.project.dto.JobPositionDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.service.JobApplicationService;
import com.se1873.js.springboot.project.service.JobPositionService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.utils.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {
  private final JobPositionService jobPositionService;
  private final JobApplicationService jobApplicationService;
  private final DepartmentService departmentService;
  private final DepartmentRepository departmentRepository;
  private final StringUtils stringUtils;

  private static final String RECRUITMENT_APPLICATION_URL = "/recruitment/applications/";
  private static final String CONTENT_FRAGMENT = "contentFragment";
  private static final String INDEX = "index";
  private static final String DEPARTMENTS = "departments";


  @GetMapping
  public String recruitmentPage(Model model) {
    List<JobPositionDTO> openPositions = jobPositionService.getOpenPositions();
    List<JobApplicationDTO> newApplications = jobApplicationService.getApplicationsByStatus("PENDING");
    List<JobApplicationDTO> interviewingApplications = jobApplicationService.getApplicationsByStatus("INTERVIEW");
    List<JobApplicationDTO> hiredApplications = jobApplicationService.getApplicationsByStatus("HIRED");

    model.addAttribute("openPositions", openPositions);
    model.addAttribute("newApplications", newApplications);
    model.addAttribute("openPositionsCount", openPositions.size());
    model.addAttribute("newApplicationsCount", newApplications.size());
    model.addAttribute("interviewingCount", interviewingApplications.size());
    model.addAttribute("hiredCount", hiredApplications.size());
    model.addAttribute(CONTENT_FRAGMENT, "fragments/recruitment-index-fragments");
    return INDEX;
  }

  @GetMapping("/positions")
  public String positionsPage(Model model,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String department) {
    List<JobPositionDTO> positions;

    if (keyword != null && !keyword.isEmpty()) {
      positions = jobPositionService.searchPositions(keyword);
    } else if (status != null && !status.isEmpty()) {
      positions = jobPositionService.getPositionsByStatus(status);
    } else if (department != null && !department.isEmpty()) {
      positions = jobPositionService.getPositionsByDepartment(department);
    } else {
      positions = jobPositionService.getAllPositions();
    }

    Map<Long, Long> applicationCounts = jobPositionService.getApplicationCounts();

    List<String> departments = jobPositionService.getAllPositions().stream()
      .map(JobPositionDTO::getDepartment)
      .distinct()
      .toList();

    model.addAttribute("positions", positions);
    model.addAttribute("applicationCounts", applicationCounts);
    model.addAttribute(DEPARTMENTS, departments);
    model.addAttribute("keyword", keyword);
    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedDepartment", department);
    return "fragments/positions-fragments";
  }

  @GetMapping("/positions/new")
  public String showCreatePositionForm(Model model) {
    model.addAttribute("jobPositionDTO", new JobPositionDTO());
    model.addAttribute(DEPARTMENTS, jobPositionService.getAllDepartments());
    model.addAttribute(CONTENT_FRAGMENT, "fragments/position-create-fragments");
    return INDEX;
  }

  @PostMapping("/positions/create")
  public String createPosition(@ModelAttribute JobPositionDTO positionDTO) {
    JobPositionDTO createdPosition = jobPositionService.createPosition(positionDTO);
    return "redirect:/recruitment/positions/" + createdPosition.getId();
  }

  @GetMapping("/positions/{id}")
  public String positionDetailPage(@PathVariable Long id, Model model) {
    JobPositionDTO position = jobPositionService.getPositionById(id);
    List<JobApplicationDTO> applications = jobApplicationService.getApplicationsByPosition(id);
    model.addAttribute("position", position);
    model.addAttribute("applications", applications);
    model.addAttribute(CONTENT_FRAGMENT, "fragments/position-detail-fragments");
    return INDEX;
  }

  @GetMapping("/applications")
  public String applicationsPage(Model model,
                                 @RequestParam(required = false) Long positionId,
                                 @RequestParam(required = false) String status) {
    List<JobApplicationDTO> applications;
    List<JobPositionDTO> positions = jobPositionService.getAllPositions();

    if (positionId != null && status != null && !status.isEmpty()) {
      applications = jobApplicationService.getApplicationsByPositionAndStatus(positionId, status);
    } else if (positionId != null) {
      applications = jobApplicationService.getApplicationsByPosition(positionId);
    } else if (status != null && !status.isEmpty()) {
      applications = jobApplicationService.getApplicationsByStatus(status);
    } else {
      applications = jobApplicationService.getAllApplications();
    }

    applications.forEach(app -> {
      JobPositionDTO position = positions.stream()
        .filter(pos -> pos.getId().equals(app.getJobPositionId()))
        .findFirst()
        .orElse(null);
      if (position != null) {
        app.setJobPositionTitle(position.getTitle());
        app.setJobPositionDepartment(position.getDepartment());
      }
    });

    model.addAttribute("applications", applications);
    model.addAttribute("positions", positions);
    model.addAttribute("selectedPositionId", positionId);
    model.addAttribute("selectedStatus", status);
    model.addAttribute(CONTENT_FRAGMENT, "fragments/applications-fragments");
    return INDEX;
  }

  @GetMapping("/applications/{id}")
  public String applicationDetailPage(@PathVariable Long id, Model model) {
    JobApplicationDTO jobApplication = jobApplicationService.getApplicationById(id);
    JobPositionDTO jobPosition = jobPositionService.getPositionById(jobApplication.getJobPositionId());
    model.addAttribute("jobPosition", jobPosition);
    model.addAttribute("jobApplication", jobApplication);
    model.addAttribute(CONTENT_FRAGMENT, "fragments/applications-detail-fragments");
    return INDEX;
  }

  @PostMapping("/applications/{id}/status")
  public String updateApplicationStatus(
    @PathVariable Long id,
    @RequestParam String status) {
    jobApplicationService.updateApplicationStatus(id, status);
    return stringUtils.getRedirectMapping(RECRUITMENT_APPLICATION_URL + id);
  }

  @GetMapping("/applications/{id}/edit")
  public String editApplicationPage(@PathVariable Long id, Model model) {
    JobApplicationDTO application = jobApplicationService.getApplicationById(id);
    model.addAttribute("jobApplication", application);
    return "fragments/application-edit-fragments";
  }

  @PostMapping("/applications/{id}/edit")
  public String updateApplication(@PathVariable Long id,
                                  @ModelAttribute JobApplicationDTO applicationDTO,
                                  RedirectAttributes redirectAttributes) {
    try {
      applicationDTO.setId(id);
      jobApplicationService.updateApplication(applicationDTO);
      redirectAttributes.addFlashAttribute("success", "Application updated successfully");
      return stringUtils.getRedirectMapping(RECRUITMENT_APPLICATION_URL + id);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to update application: " + e.getMessage());
      return stringUtils.getRedirectMapping(RECRUITMENT_APPLICATION_URL + id + "/edit");
    }
  }

  @GetMapping("/positions/{id}/edit")
  public String showEditPositionForm(@PathVariable Long id, Model model) {
    JobPositionDTO position = jobPositionService.getPositionById(id);
    if (position == null) {
      return "redirect:/recruitment/positions";
    }
    model.addAttribute("jobPositionDTO", position);
    model.addAttribute(DEPARTMENTS, jobPositionService.getAllDepartments());
    model.addAttribute(CONTENT_FRAGMENT, "fragments/position-create-fragments");
    return INDEX;
  }

  @PostMapping("/positions/{id}/edit")
  public String updatePosition(@PathVariable Long id, 
                             @Valid @ModelAttribute JobPositionDTO positionDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("messageType", "error");
      redirectAttributes.addFlashAttribute("message", bindingResult.getFieldErrors().stream()
          .map(error -> {
              switch (error.getField()) {
                  case "deadline":
                      return "Application deadline must be in the future";
                  default:
                      return error.getDefaultMessage();
              }
          })
          .collect(Collectors.joining(", ")));
      return "redirect:/recruitment/positions/" + id + "/edit";
    }
    
    positionDTO.setId(id);
    JobPositionDTO updatedPosition = jobPositionService.updatePosition(positionDTO);
    redirectAttributes.addFlashAttribute("messageType", "success");
    redirectAttributes.addFlashAttribute("message", "Position updated successfully!");
    return "redirect:/recruitment/positions/" + updatedPosition.getId();
  }

  @DeleteMapping("/positions/{id}")
  public ResponseEntity<?> deletePosition(@PathVariable Long id) {
    try {
      jobPositionService.deletePosition(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Failed to delete position: " + e.getMessage());
    }
  }

  @PostMapping("/positions/{id}/close")
  public ResponseEntity<?> closePosition(@PathVariable Long id) {
    try {
      JobPositionDTO closedPosition = jobPositionService.closePosition(id);
      return ResponseEntity.ok(closedPosition);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Failed to close position: " + e.getMessage());
    }
  }

  @PostMapping("/applications/{id}/update-status")
  public String updateApplicationStatusAndNotes(
    @PathVariable Long id,
    @RequestParam String newStatus,
    @RequestParam(required = false) String notes,
    RedirectAttributes redirectAttributes) {
    log.info("hahaha");
    try {
      jobApplicationService.updateApplicationStatusAndNotes(id, newStatus, notes);
      log.info("hahahaa");
      redirectAttributes.addFlashAttribute("success", "Application status updated successfully");
      return stringUtils.getRedirectMapping(RECRUITMENT_APPLICATION_URL + id);
    } catch (Exception e) {
      log.error(e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Failed to update application status: " + e.getMessage());
      return stringUtils.getRedirectMapping(RECRUITMENT_APPLICATION_URL + id);
    }
  }

  @GetMapping("/applications/{id}/create-employee")
  public String createEmployeeFromApplication(@PathVariable Long id, Model model) {
    JobApplicationDTO application = jobApplicationService.getApplicationById(id);
    if (application == null) {
        return "redirect:/recruitment/applications";
    }

    EmployeeDTO employeeDTO = new EmployeeDTO();
    
    // Xử lý tên
    String fullName = application.getCandidateName().trim();
    String[] nameParts = fullName.split("\\s+");
    if (nameParts.length >= 2) {
        // Lấy phần tử cuối làm last name
        String lastName = nameParts[nameParts.length - 1];
        // Lấy tất cả các phần còn lại làm first name
        String firstName = String.join(" ", Arrays.copyOfRange(nameParts, 0, nameParts.length - 1));
        employeeDTO.setEmployeeFirstName(firstName);
        employeeDTO.setEmployeeLastName(lastName);
    } else {
        // Nếu chỉ có 1 từ, đặt làm first name
        employeeDTO.setEmployeeFirstName(fullName);
        employeeDTO.setEmployeeLastName("");
    }

    employeeDTO.setEmployeePersonalEmail(application.getEmail());
    employeeDTO.setEmployeePhone(application.getPhone());
    employeeDTO.setDepartmentName(application.getJobPositionDepartment());
    employeeDTO.setPositionName(application.getJobPositionTitle());

    model.addAttribute("employeeDTO", employeeDTO);
    model.addAttribute("applicationId", id);
    model.addAttribute(CONTENT_FRAGMENT, "fragments/employee-create-fragments");
    return INDEX;
  }
} 