package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PayrollDTO {
  private Employee employee;
  private SalaryRecord salaryRecord;
}