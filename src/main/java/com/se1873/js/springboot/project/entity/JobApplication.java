package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_position_id", nullable = false)
    private JobPosition jobPosition;

    @Column(nullable = false)
    private String candidateName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String resumeUrl;

    @Column(nullable = false)
    private String coverLetter;

    @Column(nullable = false)
    private LocalDateTime appliedDate;

    @Column(nullable = false)
    private String status; // PENDING, REVIEWING, SHORTLISTED, REJECTED, HIRED

    @Column
    private String notes;
    
    // Thông tin từ resume parsing
    @Column(columnDefinition = "TEXT")
    private String experience;
    
    @Column(columnDefinition = "TEXT")
    private String education;
    
    @Column(columnDefinition = "TEXT")
    private String skills;

    @PrePersist
    protected void onCreate() {
        appliedDate = LocalDateTime.now();
    }
} 