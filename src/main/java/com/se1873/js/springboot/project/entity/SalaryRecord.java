package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "salary_records")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaryRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "salary_id")
  private Integer salaryId;
  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "month", nullable = false)
  private Integer month;

  @Column(name = "year", nullable = false)
  private Integer year;

  @Column(name = "base_salary", nullable = false)
  private Double baseSalary;

  @Column(name = "total_allowance", nullable = false)
  private Double totalAllowance;

  @Column(name = "overtime_pay", nullable = false)
  private Double overtimePay;

  @Column(name = "deductions", nullable = false)
  private Double deductions;

  @Column(name = "insurance_deduction", nullable = false)
  private Double insuranceDeduction;

  @Column(name = "tax_amount", nullable = false)
  private Double taxAmount;

  @Column(name = "net_salary", nullable = false)
  private Double netSalary;

  @Column(name = "payment_status", nullable = false)
  private String paymentStatus;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
