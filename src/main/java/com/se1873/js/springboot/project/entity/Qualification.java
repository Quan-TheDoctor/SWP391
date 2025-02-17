package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "qualifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Qualification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "qualification_id")
  private Integer qualificationId;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "qualification_type", nullable = false)
  private String qualificationType;

  @Column(name = "qualification_name", nullable = false)
  private String qualificationName;

  @Column(name = "institution", nullable = false)
  private String institution;

  @Column(name = "issue_date", nullable = false)
  private LocalDate issueDate;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    issueDate = LocalDate.now();
  }
}
