package com.se1873.js.springboot.project.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobApplicationDTO {
    private Long id;
    private Long jobPositionId;
    private String jobPositionTitle;
    private String jobPositionDepartment;
    private String candidateName;
    private String email;
    private String phone;
    private String resumeUrl;
    private String coverLetter;
    private LocalDateTime appliedDate;
    private String status;
    private String notes;

    private String experience;
    private String education;
    private String skills;
} 