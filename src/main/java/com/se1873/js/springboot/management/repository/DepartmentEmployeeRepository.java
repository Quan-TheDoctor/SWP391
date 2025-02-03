package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.entity.DepartmentEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentEmployeeRepository extends JpaRepository<DepartmentEmployee, Long> {
  @Query("SELECT de FROM DepartmentEmployee de " +
    "WHERE de.employee.employeeId = :employeeId AND de.isPresent = true")
  DepartmentEmployee findByEmployeeId(@Param("employeeId") Integer employeeId);
}
