package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "attendances")
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private int attendanceId;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "attendance_type_id")
  private AttendanceType attendanceType;

  @Column(name = "attendance_date")
  private String attendanceDate;

  @Column(name = "attendance_start_time")
  private String attendanceStartTime;

  @Column(name = "attendance_end_time")
  private String attendanceEndTime;

  @Column(name = "attendance_note")
  private String attendanceNote;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
