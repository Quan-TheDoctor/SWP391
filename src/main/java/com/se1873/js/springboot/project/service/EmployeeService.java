package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  public Set<Employee> findAllEmployees() {
    List<Employee> employeesWithHistories = employeeRepository.findAllWithEmployementHistories(Sort.by(Sort.Direction.ASC, "employeeId"));
    List<Employee> employeesWithContracts = employeeRepository.findAllWithContracts(Sort.by(Sort.Direction.ASC, "employeeId"));

    Set<Employee> employees = new HashSet<>();
    employees.addAll(employeesWithHistories);
    employees.addAll(employeesWithContracts);
    return employees;
  }
}
