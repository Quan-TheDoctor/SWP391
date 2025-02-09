package com.se1873.js.springboot.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "allowances")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Allowance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "allowance_id")
  private Integer allowanceId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "allowance_type", nullable = false)
  private String allowanceType;

  @Column(name = "amount", nullable = false)
  private Double amount;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
