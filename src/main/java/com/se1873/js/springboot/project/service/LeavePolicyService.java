package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Leave;
import com.se1873.js.springboot.project.entity.LeavePolicy;
import com.se1873.js.springboot.project.repository.LeavePolicyRepository;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveRepository leaveRepository;

    public Integer calculate(Integer leavePolicyId,Integer employeeId, RequestDTO requestDTO){
        LeavePolicy leavePolicy = leavePolicyRepository.findLeavePolicyByLeavePolicyId(leavePolicyId);
        List<Leave> allLeave = leaveRepository.findAllByEmployee_EmployeeId(employeeId);
        int totalUsedDay = allLeave.stream()
          .filter(leave -> leave.getReason().equals(leavePolicy.getLeavePolicyName()) && leave.getStatus().equals("Approved"))
          .mapToInt(Leave::getTotalDays)
          .sum();
        int remainingDays = 0;
        if(totalUsedDay == 0)  remainingDays =  leavePolicy.getLeavePolicyAmount() - requestDTO.getLeaveDTO().getTotalDays();
        else if(totalUsedDay < leavePolicy.getLeavePolicyAmount())  remainingDays = leavePolicy.getLeavePolicyAmount() - (totalUsedDay + requestDTO.getLeaveDTO().getTotalDays());
        return remainingDays;
    }
    public List<LeavePolicy> getAllLeavePolicies() {return leavePolicyRepository.findAll();}
}
