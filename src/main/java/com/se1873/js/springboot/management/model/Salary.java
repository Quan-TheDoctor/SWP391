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
  private double salary;

  @Column(name = "salary_start_date")
  private String slaryStartDate;

  @Column(name = "salary_end_date")
  private String slaryEndDate;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
