package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

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
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "department_id")
  private Integer departmentId;

  @Column(name = "department_code")
  private String departmentCode;

  @Column(name = "department_name")
  private String departmentName;

  @Column(name = "description")
  private String description;

  @Column(name = "manager_id")
  private Integer managerId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
  @ToString.Exclude
  @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
  private List<EmploymentHistory> employmentHistory = new ArrayList<>();
  @ToString.Exclude
  @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
  private List<Position> positions = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
