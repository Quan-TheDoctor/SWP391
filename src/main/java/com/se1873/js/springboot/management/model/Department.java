package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "departments")
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "department_id")
  private int departmentId;

  @Column(name = "department_name")
  private String departmentName;

  @Column(name = "department_manager_id")
  private int departmentManagerId;

  @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EmployeePosition> employeePositions;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
