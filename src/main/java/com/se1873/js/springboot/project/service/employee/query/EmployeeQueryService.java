package com.se1873.js.springboot.project.service.employee.query;

import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeQueryService {
  List<EmployeeDTO> getAll();
  Page<EmployeeDTO> getAll(Pageable pageable);
  Page<EmployeeDTO> getEmployeesByDepartmentId(Integer departmentId, Pageable pageable);
  List<Employee> getEmployeesByDepartmentId(Integer departmentId);
  EmployeeDTO getEmployeeByEmployeeId(Integer employeeId);
  List<EmployeeDTO> sort(Page<EmployeeDTO> source, String direction, String field);
  Page<EmployeeDTO> filter(String field, String value, Pageable pageable);
  Page<Employee> search(String query, Pageable pageable);
  EmployeeCountDTO getAvailableAndUnavailableEmployeeCount();
  Integer getEmployeeCount();
  Double getAverageSalary(List<EmployeeDTO> employees);
}
