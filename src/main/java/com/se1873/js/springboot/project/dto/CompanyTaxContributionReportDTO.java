package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyTaxContributionReportDTO {
  private Integer employeeId;
  private String employeeName;
  private String departmentName;
  private Double baseSalary;
  private Double healthInsurance;
  private Double socialInsurance;
  private Double unionFee;
  private Double unemploymentInsurance;
  private Double totalContribution;
  private Integer month;
  private Integer year;
}
