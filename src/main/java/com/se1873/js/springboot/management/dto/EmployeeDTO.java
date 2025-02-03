package com.se1873.js.springboot.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
  private Integer employeeId;
  private String employeeName;
  private String employeeCode;
  private String employeeAddress;
  private String employeePhone;
  private String employeeEmail;
  private int departmentId;
  private String departmentName;
  private int positionId;
  private String positionName;
  private String createdAt;
  private int roleId;
  private String roleName;
  private boolean isPresent;
  private boolean isDeleted;

  public boolean getIsPresent() {
    return isPresent;
  }
}
