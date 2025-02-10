package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_records")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "training_id")
  private Integer trainingId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Employee employee;

  @Column(name = "training_name", nullable = false)
  private String trainingName;

  @Column(name = "training_type", nullable = false)
  private String trainingType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "trainer")
  private String trainer;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "result")
  private String result;

  @Column(name = "comments")
  private String comments;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
