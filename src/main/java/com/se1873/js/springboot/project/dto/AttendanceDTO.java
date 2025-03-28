package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
  private Integer employeeId;
  private String employeeCode;
  private String employeeFirstName;
  private String employeeLastName;

  private Integer departmentId;
  private String departmentName;

  private Integer positionId;
  private String positionName;
  private String positionDescription;

  private Integer attendanceId;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate attendanceDate;
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime attendanceCheckIn;
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime attendanceCheckOut;
  private Double attendanceOvertimeHours;
  private String attendanceStatus;
  private Double attendanceWorkHours;
}
