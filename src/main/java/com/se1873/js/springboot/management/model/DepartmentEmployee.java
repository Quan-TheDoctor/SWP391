package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "department_employees")
@Data
public class DepartmentEmployee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "department_employee_id")
  private int departmentEmployeeId;

  @Column(name = "department_id")
  private int departmentId;

  @Column(name = "employee_id")
  private int employeeId;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
