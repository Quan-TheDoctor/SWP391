package com.se1873.js.springboot.management.dto;

import com.se1873.js.springboot.management.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
  private int employeeId;
  private String employeeName;
  private String employeeCode;
  private String employeeAddress;
  private String employeePhone;
  private String employeeEmail;
  private int departmentId;
  private String departmentName;
  private int positionId;
  private String positionName;
  private int roleId;
  private String roleName;
  private Integer salaryId;
  private String salaryAmount;
  private LocalDate salaryStartDate;
  private LocalDate salaryEndDate;
  private LocalDate joiningDate;
  private boolean isPresent;
  private boolean isDeleted;

  public boolean getIsPresent() {
    return isPresent;
  }
  public void setIsPresent(Boolean isPresent) {
    this.isPresent = isPresent;
  }
}
