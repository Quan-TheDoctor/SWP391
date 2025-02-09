package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.Contract;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Position;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeDTO {
  private Employee employee;
  private Department department;
  private Position position;
  private Contract contract;
}
