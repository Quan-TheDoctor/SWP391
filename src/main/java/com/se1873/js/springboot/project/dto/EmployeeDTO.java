package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.Dependent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
  private Integer employeeId;
  private String employeeCode;
  private String employeeFirstName;
  private String employeeLastName;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate employeeBirthDate;
  private String employeeGender;
  private String employeeIdNumber;
  private String employeePermanentAddress;
  private String employeeTemporaryAddress;
  private String employeePersonalEmail;
  private String employeeCompanyEmail;
  private String employeePhone;
  private String employeeMaritalStatus;
  private String employeeBankAccount;
  private String employeeBankName;
  private String employeeTaxCode;

  private Integer departmentId;
  private String departmentName;
  private String departmentDescription;

  private Integer positionId;
  private String positionName;
  private String positionDescription;

  private Integer employmentHistoryId;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate employmentHistoryStartDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate employmentHistoryEndDate;
  private Boolean employmentHistoryIsCurrent;

  private Integer contractId;
  private String contractType;
  private String contractCode;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate contractStartDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate contractEndDate;
  @NumberFormat(style = NumberFormat.Style.NUMBER)
  private Double contractBaseSalary;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate contractSignDate;
  private Boolean contractIsPresent;

  private List<Dependent> dependents;
}
