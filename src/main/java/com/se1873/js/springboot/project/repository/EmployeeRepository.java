package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Employee findByEmployeeId(Integer employeeId);

  Employee findByEmployeeCode(String employeeCode);
}
