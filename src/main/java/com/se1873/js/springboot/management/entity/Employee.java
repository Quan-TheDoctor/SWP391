package com.se1873.js.springboot.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_id")
  private int employeeId;

  @Column(name = "employee_code")
  private String employeeCode;

  @Column(name = "employee_avatar_url")
  private String employeeAvatarUrl;

  @Column(name = "employee_name")
  private String employeeName;

  @Column(name = "employee_phone")
  private String employeePhone;

  @Column(name = "employee_email")
  private String employeeEmail;

  @Column(name = "employee_address")
  private String employeeAddress;

  @Column(name = "created_at")
  private String createdAt;

  @Column(name = "is_present")
  private boolean isPresent;

  @Column(name = "is_deleted")
  private boolean isDeleted;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<DepartmentEmployee> departmentEmployees;

  public boolean getIsPresent() {
    return isPresent;
  }

}
