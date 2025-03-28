package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.Dependent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
  private Integer employeeId;
  private String employeeCode;
  @NotBlank(message = "First name cannot be blank") private String employeeFirstName;
  @NotBlank(message = "Last name cannot be blank") private String employeeLastName;
  @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate employeeBirthDate;
  private String employeeGender;
  @NotBlank(message = "ID Number cannot be blank")
  @Pattern(regexp = "^\\d{12}$", message = "ID Number must be exactly 12 digits")
  private String employeeIdNumber;
  @NotBlank(message = "Permanent Address cannot be blank") private String employeePermanentAddress;
  private String employeeTemporaryAddress;
  @NotBlank(message = "Personal Email cannot be blank") private String employeePersonalEmail;
  private String employeeCompanyEmail;
  @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
  private String employeePhone;
  private String employeeMaritalStatus;
  private String employeeBankAccount;
  private String employeeBankName;
  private String employeeTaxCode;
  private byte[] picture;
  private Boolean isDeleted = false;

  private Integer departmentId;
  private String departmentName;
  private String departmentDescription;
  private String departmentCode;
  private Integer managerId;
  private LocalDateTime departmentCreatedAt;

  private Integer positionId;
  private String positionName;
  private String positionDescription;
  private String positionCode;
  private Integer positionLevel;
  private LocalDateTime positionCreatedAt;

  private Integer employmentHistoryId;
  private Boolean employmentHistoryIsCurrent;
  private String transferReason;
  private String decisionNumber;
  private LocalDateTime employmentHistoryCreatedAt;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate employmentHistoryStartDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate employmentHistoryEndDate;

  private Integer contractId;
  private String contractType;
  private String contractCode;
  private Boolean contractIsPresent;
  private LocalDateTime contractCreatedAt;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate contractStartDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate contractEndDate;
  @NumberFormat(style = NumberFormat.Style.NUMBER, pattern = "#,##0.00") private Double contractBaseSalary;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate contractSignDate;

  private List<DependentDTO> dependents = new ArrayList<>();
}
