package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmployeeDTO {
  private Employee employee;
  private User user;
  private Department department;
  private Position position;
  private EmploymentHistory currentEmploymentHistory;
  private Contract currentContract;
  private List<EmploymentHistory> employmentHistory;
  private Set<Dependent> dependents;
  private List<Contract> contracts;
  private List<TrainingRecord> trainingRecords;
  private List<EmployeeSkill> employeeSkills;
  private List<Qualification> qualifications;
  private List<PerformanceReview> performanceReviews;
  private List<SalaryRecord> salaryRecords;
  private List<Leave> leaves;
  private List<Attendance> attendances;
  private List<Allowance> allowances;
}
