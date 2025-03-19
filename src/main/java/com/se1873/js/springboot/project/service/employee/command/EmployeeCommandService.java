package com.se1873.js.springboot.project.service.employee.command;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;

public interface EmployeeCommandService {
  void saveEmployee(EmployeeDTO dto, Department department, Position position);
}
