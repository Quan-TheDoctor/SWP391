package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.model.Employee;
import com.se1873.js.springboot.management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
  @Autowired
  private EmployeeRepository employeeRepository;

  public Employee findByEmployeeLastName(String name) {
    return employeeRepository.findByEmployeeLastName(name);
  }

  public void insertEmployee(Employee employee) {
    employeeRepository.save(employee);
  }

  public void deleteAllEmployees() {
    employeeRepository.deleteAll();
  }
}
