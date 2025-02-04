package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.dto.EmployeeDTO;
import com.se1873.js.springboot.management.entity.Employee;
import com.se1873.js.springboot.management.entity.Salary;
import com.se1873.js.springboot.management.repository.EmployeeRepository;
import com.se1873.js.springboot.management.repository.SalaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SalaryService {
  private final SalaryRepository salaryRepository;
  private final EmployeeRepository employeeRepository;

  public SalaryService(SalaryRepository salaryRepository, EmployeeRepository employeeRepository) {
    this.salaryRepository = salaryRepository;
    this.employeeRepository = employeeRepository;
  }

  public void updateSalary(Integer employeeId) {
    Salary salary = salaryRepository.findByEmployee_EmployeeIdAndIsPresent(employeeId, true);

    if (salary == null) {
      throw new RuntimeException("Salary with employeeId " + employeeId + " not found.");
    }
    salary.setEndDate(LocalDate.now());
    salary.setIsPresent(false);

    salaryRepository.save(salary);
  }

  public void save(EmployeeDTO employeeDTO) {
    Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeDTO.getEmployeeId());
    Salary salary = Salary
      .builder()
      .employee(employee)
      .salaryAmount(Double.valueOf(employeeDTO.getSalaryAmount().replace(",", "")))
      .startDate(LocalDate.now())
      .isPresent(true)
      .build();
    salaryRepository.save(salary);
  }
}
