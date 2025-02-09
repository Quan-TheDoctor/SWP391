package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
  @Query("select d from Department d " +
    "LEFT JOIN EmploymentHistory eh ON eh.department.departmentId = d.departmentId " +
    "JOIN Employee e ON e.employeeId = eh.employee.employeeId " +
    "WHERE eh.isCurrent = true AND eh.historyId = :historyId")
  Department findDepartmentByEmploymentHistoryId(@Param("historyId") Integer employmentHistoryId);
}
