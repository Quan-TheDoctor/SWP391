package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave,Long> {
  Leave findTopByEmployee_EmployeeIdAndReasonOrderByLeaveIdDesc(Integer employeeEmployeeId, String reason);

  List<Leave> findAllByEmployee_EmployeeId(Integer employeeEmployeeId);

  boolean existsByEmployee_EmployeeIdAndLeavePolicyIdAndStartDateOrEndDate(Integer employeeEmployeeId, Integer leavePolicyId, LocalDate startDate, LocalDate endDate);

  List<Leave> findLeaveByEmployee_EmployeeIdAndStartDateAndEndDate(Integer employeeEmployeeId, LocalDate startDate, LocalDate endDate);

    Page<Leave> getLeaveByLeaveType(String leaveType, Pageable pageable);
}
