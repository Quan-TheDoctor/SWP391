package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employees")
@Data
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_id")
  private int employeeId;

  @Column(name = "user_id")
  private int userId;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "phone")
  private String phone;

  @Column(name = "salary")
  private double salary;

  @Column(name = "duration")
  private int duration;

  @Column(name = "contract_start_time")
  private String contractStartTime;

  @Column(name = "contract_end_time")
  private String contractEndTime;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
