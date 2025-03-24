package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollCalculationDTO {
  private Integer employeeId;
  private String employeeFistName;
  private String employeeLastName;
  private int workedDays;
  private int lateDays;
  private int absentDays;
  private double overtimeHours;
}