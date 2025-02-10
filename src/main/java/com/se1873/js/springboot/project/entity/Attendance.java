package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private Integer attendanceId;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "check_in")
  private LocalDateTime checkIn;

  @Column(name = "check_out")
  private LocalDateTime checkOut;

  @Column(name = "status")
  private String status;

  @Column(name = "overtime_hours")
  private Double overtimeHours;

  @Column(name = "note")
  private String note;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
