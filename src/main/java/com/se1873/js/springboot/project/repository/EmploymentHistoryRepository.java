package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.EmploymentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, Long> {
  EmploymentHistory findEmploymentHistoryByIsCurrentAndEmployee_EmployeeId(Boolean isCurrent, Integer employeeEmployeeId);

  @Query("SELECT eh FROM EmploymentHistory eh WHERE eh.department.departmentId = :departmentId AND eh.isCurrent = true")
  Page<EmploymentHistory> findEmployeeByDepartmentID(@Param("departmentId") Integer departmentId, Pageable pageable);

  @Query("SELECT eh FROM EmploymentHistory eh WHERE eh.position.positionId = :positionId AND eh.isCurrent = true")
  Page<EmploymentHistory> findEmployeeByPositionId(@Param("positionId") Integer positionId, Pageable pageable);
}