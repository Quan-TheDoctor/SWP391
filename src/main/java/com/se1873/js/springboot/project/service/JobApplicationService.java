package com.se1873.js.springboot.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se1873.js.springboot.project.dto.JobApplicationDTO;
import com.se1873.js.springboot.project.entity.JobApplication;
import com.se1873.js.springboot.project.entity.JobPosition;
import com.se1873.js.springboot.project.repository.JobApplicationRepository;
import com.se1873.js.springboot.project.repository.JobPositionRepository;
import com.se1873.js.springboot.project.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobPositionRepository jobPositionRepository;
    private final ObjectMapper objectMapper;
    private final EmailUtils emailUtils;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                               JobPositionRepository jobPositionRepository,
                               EmailUtils emailUtils) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.objectMapper = new ObjectMapper();
        this.emailUtils = emailUtils;
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
        JobPosition jobPosition = jobPositionRepository.findById(dto.getJobPositionId())
            .orElseThrow(() -> {
                log.error("Job position not found with ID: {}", dto.getJobPositionId());
                return new RuntimeException("Job position not found");
            });
        JobApplication jobApplication = new JobApplication();
        jobApplication.setJobPosition(jobPosition);
        jobApplication.setCandidateName(dto.getCandidateName());
        jobApplication.setEmail(dto.getEmail());
        jobApplication.setPhone(dto.getPhone());
        jobApplication.setResumeUrl(dto.getResumeUrl());
        jobApplication.setCoverLetter(dto.getCoverLetter());
        jobApplication.setStatus("Pending");
        jobApplication.setNotes(dto.getNotes());

        if (dto.getExperience() != null) {
            try {
                List<Map<String, String>> experiences = objectMapper.readValue(dto.getExperience(), List.class);
                StringBuilder experienceBuilder = new StringBuilder();
                for (Map<String, String> exp : experiences) {
                    experienceBuilder.append("Công ty: ").append(exp.get("company")).append("\n");
                    experienceBuilder.append("Vị trí: ").append(exp.get("jobTitle")).append("\n");
                    experienceBuilder.append("Thời gian: ").append(exp.get("duration")).append("\n");
                    experienceBuilder.append("Mô tả: ").append(exp.get("description")).append("\n");
                    experienceBuilder.append("-------------------\n");
                }
                jobApplication.setExperience(experienceBuilder.toString());
            } catch (Exception e) {
                log.error("Error parsing experience JSON: ", e);
                jobApplication.setExperience(dto.getExperience());
            }
        }

        if (dto.getEducation() != null) {
            try {
                List<Map<String, String>> educations = objectMapper.readValue(dto.getEducation(), List.class);
                StringBuilder educationBuilder = new StringBuilder();
                for (Map<String, String> edu : educations) {
                    educationBuilder.append("Bằng cấp: ").append(edu.get("degree")).append("\n");
                    educationBuilder.append("Trường học: ").append(edu.get("institution")).append("\n");
                    educationBuilder.append("Năm tốt nghiệp: ").append(edu.get("years")).append("\n");
                    educationBuilder.append("GPA: ").append(edu.get("gpa")).append("\n");
                    educationBuilder.append("-------------------\n");
                }
                jobApplication.setEducation(educationBuilder.toString());
            } catch (Exception e) {
                log.error("Error parsing education JSON: ", e);
                jobApplication.setEducation(dto.getEducation());
            }
        }

        jobApplication.setSkills(dto.getSkills());

        JobApplication savedApplication = jobApplicationRepository.save(jobApplication);
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
        if (!isValidStatusTransition(currentStatus.trim(), newStatus.trim())) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        application.setStatus(newStatus.trim());
        if (notes != null && !notes.trim().isEmpty()) {
            String currentNotes = application.getNotes();
            String newNoteEntry = String.format("\n[%s] [%s] %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                newStatus.trim(),
                notes.trim());
            
            if (currentNotes == null || currentNotes.trim().isEmpty()) {
                application.setNotes(newNoteEntry);
            } else {
                application.setNotes(currentNotes + "\n" + newNoteEntry);
            }
        }
        
        jobApplicationRepository.save(application);

        emailUtils.sendStatusUpdateEmail(
            application.getEmail(),
            application.getCandidateName(),
            application.getJobPosition().getTitle(),
            newStatus.trim()
        );
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
        dto.setMatchRate(calculateMatchRate(jobApplication));
        return dto;
    }

    private int calculateMatchRate(JobApplication application) {
        String requirements = application.getJobPosition().getRequirements().toLowerCase();
        String skills = application.getSkills() != null ? application.getSkills().toLowerCase() : "";
        String experience = application.getExperience() != null ? application.getExperience().toLowerCase() : "";
        String education = application.getEducation() != null ? application.getEducation().toLowerCase() : "";

        String[] requirementLines = requirements.split("\n");
        int totalRequirements = requirementLines.length;
        int matchedRequirements = 0;

        for (String requirement : requirementLines) {
            requirement = requirement.trim().toLowerCase();
            if (requirement.isEmpty()) continue;

            if (skills.contains(requirement) ||
                experience.contains(requirement) || 
                education.contains(requirement)) {
                matchedRequirements++;
            }
        }

        if (totalRequirements == 0) return 0;
        return (int) ((matchedRequirements * 100.0) / totalRequirements);
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