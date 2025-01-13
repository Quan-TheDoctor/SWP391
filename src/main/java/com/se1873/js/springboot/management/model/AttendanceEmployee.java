package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attendance_employees")
@Data
public class AttendanceEmployee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_employee_id")
  private int attendanceEmployeeId;

  @Column(name = "attendance_id")
  private int attendanceId;

  @Column(name = "employee_id")
  private int employeeId;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
