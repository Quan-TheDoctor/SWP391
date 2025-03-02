package com.se1873.js.springboot.project.dto;

public record PayrollCalculationDTO(
  Integer employeeId,
  String employeeFistName,
  String employeeLastName,
  int workedDays,
  int lateDays,
  int absentDays,
  double overtimeHours
) {}