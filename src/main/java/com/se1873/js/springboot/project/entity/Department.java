package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Department {
  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "department_id")
  private Integer departmentId;

  @Column(name = "department_code")
  private String departmentCode;

  @Column(name = "department_name")
  private String departmentName;

  @Column(name = "parent_department_id")
  private Integer parentDepartmentId;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
  private List<EmploymentHistory> employmentHistory = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
