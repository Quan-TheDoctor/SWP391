package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departments")
@Data
public class Departments {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "department_id")
  private int departmentId;

  @Column(name = "department_name")
  private String departmentName;

  @Column(name = "manager_id")
  private int managerId;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
