package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  @Transactional
  public Employee findEmployeeByEmployeeId(Integer employeeId) {
    Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeId);
    employee.getDependents();
    employee.getContracts();
    employee.getEmployeeSkills();
    employee.getEmploymentHistories();
    employee.getQualifications();
    return employee;
  }

  public Set<EmployeeDTO> findAllEmployees() {
    List<Employee> employeesWithHistories = employeeRepository.findAllWithEmploymentHistories(Sort.by(Sort.Direction.ASC, "employeeId"));
    List<Employee> employeesWithContracts = employeeRepository.findAllWithContracts(Sort.by(Sort.Direction.ASC, "employeeId"));

    Set<Employee> employees = new HashSet<>();
    employees.addAll(employeesWithHistories);
    employees.addAll(employeesWithContracts);
    return employees.stream().map(this::convertEmployeeToEmployeeDTO).collect(Collectors.toSet());
  }

  public Set<Employee> sortEmployeeSByFieldAndDirection(String field, String direction) {
    Sort sort = direction.equals("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
    List<Employee> employeesWithHistories = employeeRepository.findAllWithEmploymentHistories(sort);
    List<Employee> employeesWithContracts = employeeRepository.findAllWithContracts(sort);

    Set<Employee> employees = new HashSet<>();
    employees.addAll(employeesWithHistories);
    employees.addAll(employeesWithContracts);
    return employees;
  }

  private EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
    EmploymentHistory employmentHistory = employee.getEmploymentHistories()
      .stream()
      .filter(history -> history.getIsCurrent())
      .findFirst()
      .get();
    return EmployeeDTO
      .builder()
      .employee(employee)
      .department(employmentHistory.getDepartment())
      .position(employmentHistory.getPosition())
      .build();
  }
}
