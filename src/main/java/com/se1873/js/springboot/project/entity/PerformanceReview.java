package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_id")
  private Integer reviewId;
  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id")
  private Employee reviewer;

  @Column(name = "review_period", nullable = false)
  private String reviewPeriod;

  @Column(name = "review_year", nullable = false)
  private Integer reviewYear;

  @Column(name = "overall_score", nullable = false)
  private Double overallScore;

  @Column(name = "strengths")
  private String strengths;

  @Column(name = "improvements")
  private String improvements;

  @Column(name = "comments")
  private String comments;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
