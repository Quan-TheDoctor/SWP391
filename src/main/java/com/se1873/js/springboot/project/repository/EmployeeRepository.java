package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Employee getEmployeeByEmployeeId(Integer employeeId);

  Employee findEmployeeByEmployeeId(Integer employeeId);
}
