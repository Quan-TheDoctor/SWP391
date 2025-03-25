package com.se1873.js.springboot.project.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobPositionDTO {
    private Long id;
    private String title;
    private String department;
    private String description;
    private String requirements;
    private String responsibilities;
    private String salaryRange;
    private Integer numberOfPositions;
    private LocalDateTime postedDate;
    private LocalDateTime deadline;
    private String status;
    private Integer applicationCount;
    private boolean isDeleted;
} 