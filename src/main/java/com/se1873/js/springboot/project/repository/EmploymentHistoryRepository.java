package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.EmploymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, Long> {
  EmploymentHistory findEmploymentHistoryByIsCurrentAndEmployee_EmployeeId(Boolean isCurrent, Integer employeeEmployeeId);

  @Query("SELECT eh FROM EmploymentHistory eh WHERE eh.department.departmentId = :departmentId AND eh.isCurrent = true")
  List<EmploymentHistory> finds(@Param("departmentId") Integer departmentId);
}