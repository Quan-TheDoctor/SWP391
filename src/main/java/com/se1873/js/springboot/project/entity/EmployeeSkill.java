package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_skills")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSkill {
  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "skill_id")
  private Integer skillId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "skill_name", nullable = false)
  private String skillName;

  @Column(name = "proficiency_level", nullable = false)
  private String proficiencyLevel;

  @Column(name = "years_of_experience")
  private Double yearsOfExperience;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
