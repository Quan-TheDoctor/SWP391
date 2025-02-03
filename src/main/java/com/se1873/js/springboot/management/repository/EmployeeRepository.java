package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  Employee findEmployeeByEmployeeId(int employeeId);
}
