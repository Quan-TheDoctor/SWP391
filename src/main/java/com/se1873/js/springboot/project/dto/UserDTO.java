package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
  private Integer userId;
  private String username;
  private String role;
  private LocalDateTime updatedAt;
  private LocalDateTime createdAt;
  private Employee employee;
  private List<AuditLog> auditLogs;

  private Integer employeeId;
  private String employeeCode;
  private String employeeFirstName;
  private String employeeLastName;
  private String employeeGender;
  private LocalDate employeeBirthDate;
  private String employeeBankAccount;
  private String employeeBankName;
  private String employeeTemporaryAddress;
  private String employeePermanentAddress;
  private String employeeIdNumber;
  private String employeeTaxCode;
  private String employeePersonalEmail;
  private String employeeCompanyEmail;
  private String employeePhone;
  private String employeeMaritalStatus;
}
