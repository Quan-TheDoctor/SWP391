package com.se1873.js.springboot.project.service;


import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.dto.LeaveDTO;
import com.se1873.js.springboot.project.entity.Leave;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;

    public Page<Leave> getAllLeave(Pageable pageable) {
        return leaveRepository.findAll(pageable);
    }

    public Page<Leave> filterLeaveByLeaveType(String leaveType, Pageable pageable){
        return leaveRepository.getLeaveByLeaveType(leaveType, pageable);
    }


    public Page<Leave> sortLeave(Page<Leave> leaveList, Pageable pageable, String field, String direction) {
        if (leaveList == null) leaveList = getAllLeave(pageable);

        Comparator<Leave> comparator = null;
        if("asc".equals(direction)){
            comparator = Comparator.comparing(leave -> (Comparable) getFieldValue(leave, field),
                    Comparator.naturalOrder());
        } else if("desc".equals(direction)){
            comparator = Comparator.comparing(leave -> (Comparable) getFieldValue(leave, field),
                    Comparator.reverseOrder());
        }


        List<Leave> sortedList = leaveList.stream().sorted(comparator).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedList.size());
        List<Leave> pagedList = sortedList.subList(start, end);

        return new PageImpl<>(pagedList, pageable, sortedList.size());
    }

    private Object getFieldValue(Leave leave, String field) {
        return switch (field) {
            case "employeeName" -> leave.getEmployee().getFirstName() + leave.getEmployee().getLastName();
            case "startDate" -> leave.getStartDate();
            case "endDate" -> leave.getEndDate();
            case "totalDays" -> leave.getTotalDays();
            default -> throw new IllegalArgumentException("Invalid field: " + field);
        };
    }

    public LeaveDTO getLeaveDTO(int employeeId, Leave leave) {
        if (leave == null) {
            throw new IllegalArgumentException("Leave object cannot be null");
        }

        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
        if (employeeDTO == null) {
            throw new IllegalArgumentException("Employee not found for ID: " + employeeId);
        }



        return LeaveDTO.builder()
                .leaveId(leave.getLeaveId())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .reason(leave.getReason())
                .leaveType(leave.getLeaveType())
                .leaveAllowedDay(leave.getLeaveAllowedDay())
                .employee(employeeDTO)
                .build();
    }


}
