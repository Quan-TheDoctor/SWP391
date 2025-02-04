package com.se1873.js.springboot.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "salaries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Salary {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "salary_id")
  private Integer salaryId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "salary_amount")
  private Double salaryAmount;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "is_present")
  private Boolean isPresent;

  public boolean getIsPresent() {
    return isPresent;
  }
  public void setIsPresent(Boolean isPresent) { this.isPresent = isPresent; }
}
