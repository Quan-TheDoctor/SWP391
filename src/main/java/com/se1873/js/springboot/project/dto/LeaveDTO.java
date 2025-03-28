package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveDTO {
    private int leaveId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private Integer leaveAllowedDay;
    private Integer leavePolicyId;
    private Integer DepartmentId;
    private String Position;
    private String employeeName;
    private EmployeeDTO employee;



}
