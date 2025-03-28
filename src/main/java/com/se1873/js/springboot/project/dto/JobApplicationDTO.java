package com.se1873.js.springboot.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobApplicationDTO {
    private Long id;
    
    @NotNull(message = "Position is required")
    private Long jobPositionId;
    
    private String jobPositionTitle;
    private String jobPositionDepartment;
    
    @NotBlank(message = "Candidate name is required")
    private String candidateName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
    
    private String resumeUrl;
    private String coverLetter;
    
    private LocalDateTime appliedDate;
    private String status;
    private String notes;

    private String experience;
    private String education;
    private String skills;
    private Integer matchRate;
} 