package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attendances")
@Data
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private int attendanceId;

  @Column(name = "date")
  private String date;

  @Column(name = "start_time")
  private String startTime;

  @Column(name = "end_time")
  private String endTime;

  @Column(name = "attendance_status")
  private String attendanceStatus;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
