package com.se1873.js.springboot.project.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDTO {
  private Integer employeeId;
  private String employeeFirstName;
  private String employeeLastName;

  private Integer salaryRecordId;
  private Integer salaryRecordMonth;
  private Integer salaryRecordYear;
  private Double salaryRecordBaseSalary;
  private Double salaryRecordTotalAllowance;
  private Double salaryRecordOvertimePay;
  private Double salaryRecordDeductions;
  private Double salaryRecordInsuranceDeduction;
  private Double salaryRecordTaxAmount;
  private Double salaryRecordNetSalary;
  private String salaryRecordPaymentStatus;
}
