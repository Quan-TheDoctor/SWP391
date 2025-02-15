package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
  private Employee employee;
  private EmploymentHistory employmentHistory;
  private Department department;
  private Position position;
  private Contract contract;
  private List<Qualification> qualifications;
  private List<EmploymentHistory> employmentHistories;
  private List<Attendance> attendances;
  private User user;
}
