package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findAllByEmployee_EmployeeId(Integer employeeId);

    Leave findTopByEmployee_EmployeeIdAndReasonOrderByLeaveIdDesc(Integer employeeId, String reason);

    List<Leave> findLeaveByEmployee_EmployeeIdAndStartDateAndEndDate(Integer employeeId, LocalDate startDate, LocalDate endDate);

        @Query("SELECT COUNT(l) > 0 FROM Leave l WHERE l.employee.employeeId = :employeeId AND l.leavePolicyId = :leavePolicyId AND (l.startDate = :startDate OR l.endDate = :endDate)")
        boolean existsByEmployeeIdAndLeavePolicyIdAndStartDateOrEndDate(
                @Param("employeeId") Integer employeeId,
                @Param("leavePolicyId") Integer leavePolicyId,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );
}
