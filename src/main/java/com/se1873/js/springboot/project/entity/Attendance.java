package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Column(name = "date", nullable = false)
  private LocalDate date;
  @DateTimeFormat(pattern = "HH:mm:ss")
  @Column(name = "check_in")
  private LocalTime checkIn;
  @DateTimeFormat(pattern = "HH:mm:ss")
  @Column(name = "check_out")
  private LocalTime checkOut;

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
