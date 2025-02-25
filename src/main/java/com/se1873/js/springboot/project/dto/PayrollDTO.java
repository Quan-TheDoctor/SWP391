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
  private String employeeTaxCode;

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

  private Double calculatedEmployeeHealthInsurance;
  private Double calculatedEmployeeHealthInsuranceAmount;
  private Double calculatedEmployeeSocialInsurance;
  private Double calculatedEmployeeSocialInsuranceAmount;
  private Double calculatedEmployeeUnionFee;
  private Double calculatedEmployeeUnionFeeAmount;
  private Double calculatedEmployeeUnemploymentInsurance;
  private Double calculatedEmployeeUnemploymentInsuranceAmount;
  private Double calculatedEmployerHealthInsurance;
  private Double calculatedEmployerHealthInsuranceAmount;
  private Double calculatedEmployerSocialInsurance;
  private Double calculatedEmployerSocialInsuranceAmount;
  private Double calculatedEmployerUnionFee;
  private Double calculatedEmployerUnionFeeAmount;
  private Double calculatedEmployerUnemploymentInsurance;
  private Double calculatedEmployerUnemploymentInsuranceAmount;
  private Double calculatedPersonalInsuranceDeduction;
  private Double calculatedPersonalDeduction;
  private Double calculatedPersonalDependentDeduction;
  private Double totalGrossSalary;
  private Double totalDeductions;

  private Double totalTaxAmount;
  private Double totalNetSalary;
}
