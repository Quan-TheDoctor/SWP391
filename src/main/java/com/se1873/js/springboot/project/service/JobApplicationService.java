package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.JobApplicationDTO;
import com.se1873.js.springboot.project.entity.JobApplication;
import com.se1873.js.springboot.project.entity.JobPosition;
import com.se1873.js.springboot.project.repository.JobApplicationRepository;
import com.se1873.js.springboot.project.repository.JobPositionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobPositionRepository jobPositionRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                               JobPositionRepository jobPositionRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobPositionRepository = jobPositionRepository;
    }

    public List<JobApplicationDTO> getApplicationsByPosition(Long positionId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobPositionId(positionId);
        return jobApplications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsByStatus(String status) {
        return jobApplicationRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsByPositionAndStatus(Long positionId, String status) {
        return jobApplicationRepository.findByJobPositionIdAndStatus(positionId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public JobApplicationDTO getApplicationById(Long id) {
        return jobApplicationRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public void updateApplicationStatus(Long id, String status) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        jobApplicationRepository.save(application);
    }

    @Transactional
    public JobApplication saveJobApplication(JobApplicationDTO dto) {
        log.info("Starting to save job application for candidate: {}", dto.getCandidateName());
        
        log.debug("Finding job position with ID: {}", dto.getJobPositionId());
        JobPosition jobPosition = jobPositionRepository.findById(dto.getJobPositionId())
            .orElseThrow(() -> {
                log.error("Job position not found with ID: {}", dto.getJobPositionId());
                return new RuntimeException("Job position not found");
            });
        log.debug("Found job position: {}", jobPosition.getTitle());

        log.debug("Creating new job application entity");
        JobApplication jobApplication = new JobApplication();
        jobApplication.setJobPosition(jobPosition);
        jobApplication.setCandidateName(dto.getCandidateName());
        jobApplication.setEmail(dto.getEmail());
        jobApplication.setPhone(dto.getPhone());
        jobApplication.setResumeUrl(dto.getResumeUrl());
        jobApplication.setCoverLetter(dto.getCoverLetter());
        jobApplication.setStatus("Pending");
        jobApplication.setNotes(dto.getNotes());

        jobApplication.setExperience(dto.getExperience());
        jobApplication.setEducation(dto.getEducation());
        jobApplication.setSkills(dto.getSkills());

        log.debug("Saving job application to database");
        JobApplication savedApplication = jobApplicationRepository.save(jobApplication);
        log.info("Successfully saved job application with ID: {}", savedApplication.getId());

        return savedApplication;
    }

    public List<JobApplicationDTO> getAllApplications() {
        return jobApplicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void updateApplication(JobApplicationDTO applicationDTO) {
        JobApplication application = jobApplicationRepository.findById(applicationDTO.getId())
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setCandidateName(applicationDTO.getCandidateName());
        application.setEmail(applicationDTO.getEmail());
        application.setPhone(applicationDTO.getPhone());
        application.setResumeUrl(applicationDTO.getResumeUrl());
        application.setCoverLetter(applicationDTO.getCoverLetter());
        application.setStatus(applicationDTO.getStatus());
        application.setExperience(applicationDTO.getExperience());
        application.setEducation(applicationDTO.getEducation());
        application.setSkills(applicationDTO.getSkills());
        application.setNotes(applicationDTO.getNotes());
        
        jobApplicationRepository.save(application);
    }

    @Transactional
    public void updateApplicationStatusAndNotes(Long id, String newStatus, String notes) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        String currentStatus = application.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        application.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            application.setNotes(notes);
        }
        
        jobApplicationRepository.save(application);
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        return switch (currentStatus) {
            case "Pending" -> newStatus.equals("Reviewing") || newStatus.equals("Rejected");
            case "Reviewing" -> newStatus.equals("Interviewing") || newStatus.equals("Rejected");
            case "Interviewing" -> newStatus.equals("Hired") || newStatus.equals("Rejected");
            case "Hired", "Rejected" -> false;
            default -> false;
        };
    }

    private JobApplicationDTO convertToDTO(JobApplication jobApplication) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(jobApplication.getId());
        dto.setJobPositionId(jobApplication.getJobPosition().getId());
        dto.setJobPositionTitle(jobApplication.getJobPosition().getTitle());
        dto.setJobPositionDepartment(jobApplication.getJobPosition().getDepartment());
        dto.setCandidateName(jobApplication.getCandidateName());
        dto.setEmail(jobApplication.getEmail());
        dto.setPhone(jobApplication.getPhone());
        dto.setResumeUrl(jobApplication.getResumeUrl());
        dto.setCoverLetter(jobApplication.getCoverLetter());
        dto.setStatus(jobApplication.getStatus());
        dto.setAppliedDate(jobApplication.getAppliedDate());
        dto.setNotes(jobApplication.getNotes());
        dto.setExperience(jobApplication.getExperience());
        dto.setEducation(jobApplication.getEducation());
        dto.setSkills(jobApplication.getSkills());
        return dto;
    }

    public JobApplicationDTO getJobApplicationById(Long id) {
        JobApplication jobApplication = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job Application not found"));
        
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(jobApplication.getId());
        dto.setCandidateName(jobApplication.getCandidateName());
        dto.setEmail(jobApplication.getEmail());
        dto.setPhone(jobApplication.getPhone());
        dto.setJobPositionId(jobApplication.getJobPosition().getId());
        dto.setJobPositionDepartment(jobApplication.getJobPosition().getDepartment());
        dto.setStatus(jobApplication.getStatus());
        dto.setNotes(jobApplication.getNotes());
        
        return dto;
    }
} 