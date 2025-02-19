package com.se1873.js.springboot.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "positions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Position {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "position_id")
  private Integer positionId;

  @ManyToOne(cascade = CascadeType.ALL)
  @JsonIgnore
  @JoinColumn(name = "department_id")
  private Department department;
  @JsonProperty("positionName")
  @Column(name = "position_name", nullable = false)
  private String positionName;

  @JsonIgnore
  @Column(name = "position_code", unique = true, nullable = false)
  private String positionCode;

  @JsonIgnore
  @Column(name = "level", nullable = false)
  private Integer level;

  @JsonIgnore
  @Column(name = "description")
  private String description;

  @JsonIgnore
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ToString.Exclude
  @JsonIgnore
  @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
  private List<EmploymentHistory> employmentHistory = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
