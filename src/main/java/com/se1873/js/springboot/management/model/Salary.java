package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "salaries")
public class Salary {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "salary_id")
  private int id;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "salary_amount")
  private double salaryAmount;

  @Column(name = "salary_start_date", columnDefinition = "DATE")
  private String slaryStartDate;

  @Column(name = "salary_end_date", columnDefinition = "DATE")
  private String slaryEndDate;

  @Column(name = "is_deleted", columnDefinition = "BIT")
  private boolean isDeleted = Boolean.FALSE;
}
