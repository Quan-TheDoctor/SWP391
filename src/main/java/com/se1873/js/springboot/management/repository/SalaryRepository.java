package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryRepository extends JpaRepository<Salary, Long> {
  Salary findByEmployee_EmployeeIdAndIsPresent(int employeeEmployeeId, Boolean isPresent);
}
