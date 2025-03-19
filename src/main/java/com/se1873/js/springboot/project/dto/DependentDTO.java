package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependentDTO {
  private Integer dependentId;
  private String fullName;
  private String relationship;
  private LocalDate birthDate;
  private String idNumber;
  private Boolean isTaxDependent;
  private Integer employeeId;
}
