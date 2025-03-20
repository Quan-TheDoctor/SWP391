package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.EmploymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, Long> {
  EmploymentHistory findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(Integer employeeEmployeeId, Boolean isCurrent);

  EmploymentHistory findEmploymentHistoryByHistoryId(Integer historyId);
  @Query("SELECT eh FROM EmploymentHistory eh WHERE eh.employee.employeeId = :employeeId AND eh.isCurrent = true ORDER BY eh.startDate DESC")
  List<EmploymentHistory> findCurrentEmploymentHistories(@Param("employeeId") Integer employeeId);
}
