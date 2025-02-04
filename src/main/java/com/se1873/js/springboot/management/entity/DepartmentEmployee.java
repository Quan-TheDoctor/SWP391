package com.se1873.js.springboot.management.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "department_employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentEmployee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "department_employee_id")
  private Integer departmentEmployeeId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id")
  private Role role;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "position_id")
  private Position position;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "created_at")
  private String createdAt;

  @Column(name = "is_present")
  private boolean isPresent;

  @Column(name = "is_deleted")
  private boolean isDeleted;

  public boolean getIsPresent() {
    return isPresent;
  }
  public void setIsPresent(Boolean isPresent) {
    this.isPresent = isPresent;
  }
}
