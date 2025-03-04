package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDTO {
  private Integer departmentId;
  private String departmentName;
  private String departmentDescription;
  private String departmentCode;
  private LocalDateTime departmentCreatedAt;
}
