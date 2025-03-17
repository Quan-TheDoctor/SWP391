package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaves")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Leave {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "leave_id")
  private Integer leaveId;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "leave_type", nullable = false)
  private String leaveType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "total_days", nullable = false)
  private Integer totalDays;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "reason")
  private String reason;

  @Column(name = "leave_allowed_day")
  private Integer leaveAllowedDay;

  @Column(name = "leave_policy_id")
  private Integer leavePolicyId;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by")
  private Employee approval;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
