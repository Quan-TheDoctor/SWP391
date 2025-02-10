package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.EmploymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, Long> {
  EmploymentHistory findEmploymentHistoryByIsCurrentAndEmployee_EmployeeId(Boolean isCurrent, Integer employeeEmployeeId);
}
