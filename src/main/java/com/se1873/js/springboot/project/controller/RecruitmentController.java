package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.JobApplicationDTO;
import com.se1873.js.springboot.project.dto.JobPositionDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.service.JobApplicationService;
import com.se1873.js.springboot.project.service.JobPositionService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {
    private final JobPositionService jobPositionService;
    private final JobApplicationService jobApplicationService;
    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;

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
        model.addAttribute("contentFragment", "fragments/recruitment-index-fragments");
        return "index";
    }

    @GetMapping("/positions")
    public String positionsPage(Model model) {
        List<JobPositionDTO> positions = jobPositionService.getAllPositions();
        model.addAttribute("positions", positions);
        return "recruitment/positions";
    }

    @GetMapping("/positions/create")
    public String showCreatePositionForm(Model model) {
        model.addAttribute("jobPositionDTO", new JobPositionDTO());
        model.addAttribute("departments", jobPositionService.getAllDepartments());
        model.addAttribute("contentFragment", "fragments/position-create-fragments");
        return "index";
    }

    @PostMapping("/positions/create")
    public String createPosition(@ModelAttribute JobPositionDTO position) {
        JobPositionDTO createdPosition = jobPositionService.createPosition(position);
        return "redirect:/recruitment/positions/" + createdPosition.getId();
    }

    @GetMapping("/positions/{id}")
    public String positionDetailPage(@PathVariable Long id, Model model) {
        JobPositionDTO position = jobPositionService.getPositionById(id);
        List<JobApplicationDTO> applications = jobApplicationService.getApplicationsByPosition(id);
        model.addAttribute("position", position);
        model.addAttribute("applications", applications);
        model.addAttribute("contentFragment", "fragments/position-detail-fragments");
        return "index";
    }

    @GetMapping("/applications")
    public String applicationsPage(
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String status,
            Model model) {
        List<JobApplicationDTO> applications;
        if (positionId != null && status != null) {
            applications = jobApplicationService.getApplicationsByPositionAndStatus(positionId, status);
        } else if (positionId != null) {
            applications = jobApplicationService.getApplicationsByPosition(positionId);
        } else if (status != null) {
            applications = jobApplicationService.getApplicationsByStatus(status);
        } else {
            applications = jobApplicationService.getApplicationsByStatus("PENDING");
        }
        
        List<JobPositionDTO> positions = jobPositionService.getAllPositions();
        model.addAttribute("applications", applications);
        model.addAttribute("positions", positions);
        model.addAttribute("selectedPositionId", positionId);
        model.addAttribute("status", status);
        model.addAttribute("contentFragment", "fragments/applications-fragments");
        return "index";
    }

    @GetMapping("/applications/{id}")
    public String applicationDetailPage(@PathVariable Long id, Model model) {
        JobApplicationDTO jobApplication = jobApplicationService.getApplicationById(id);
        JobPositionDTO jobPosition = jobPositionService.getPositionById(jobApplication.getJobPositionId());
        model.addAttribute("jobPosition", jobPosition);
        model.addAttribute("jobApplication", jobApplication);
        model.addAttribute("contentFragment", "fragments/applications-detail-fragments");
        return "index";
    }

    @PostMapping("/applications/{id}/status")
    public String updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        jobApplicationService.updateApplicationStatus(id, status);
        return "redirect:/recruitment/applications/" + id;
    }
} 