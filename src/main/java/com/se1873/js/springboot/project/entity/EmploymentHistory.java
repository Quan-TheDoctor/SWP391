package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employment_history")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "history_id")
  private Integer historyId;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "position_id")
  private Position position;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "is_current")
  private Boolean isCurrent;

  @Column(name = "transfer_reason")
  private String transferReason;

  @Column(name = "decision_number")
  private String decisionNumber;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
