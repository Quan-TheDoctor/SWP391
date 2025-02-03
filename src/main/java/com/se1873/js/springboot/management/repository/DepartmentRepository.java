package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  @Query("SELECT d FROM Department d " +
    "JOIN DepartmentEmployee de ON de.department.departmentId = d.departmentId " +
    "JOIN Employee e ON e.employeeId = de.employee.employeeId " +
    "WHERE e.employeeId = :employeeId AND de.isPresent = true")
  Department findDepartmentByEmployeeId(@Param("employeeId") Integer employeeId);

  Department findDepartmentByDepartmentId(int departmentId);
}
