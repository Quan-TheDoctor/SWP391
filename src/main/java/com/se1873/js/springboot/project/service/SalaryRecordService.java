package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryRecordService {

  private final SalaryRecordRepository salaryRecordRepository;
  private final AuditLogService auditLogService;
  private final UserRepository userRepository;

  public Page<PayrollDTO> findAll(Pageable pageable) {
    auditLogService.save(AuditLog.builder()
      .user(userRepository.findByUserId(1))
      .actionInfo("Select all payrolls")
      .actionType("SELECT")
      .build());
    return salaryRecordRepository.findAll(pageable).map(this::convertSalaryRecordToPayrollDTO);
  }

  public Page<PayrollDTO> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate, Pageable pageable) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }

    int startYear = startDate.getYear();
    int startMonth = startDate.getMonthValue();
    int endYear = endDate.getYear();
    int endMonth = endDate.getMonthValue();



    auditLogService.save(AuditLog.builder()
      .user(userRepository.findByUserId(1))
        .actionInfo("Filter payrolls by date range")
        .actionType("SELECT")
      .build());

    return salaryRecordRepository.findByYearMonthRange(
      startYear,
      startMonth,
      endYear,
      endMonth,
      pageable
    ).map(this::convertSalaryRecordToPayrollDTO);
  }

  public Page<PayrollDTO> findByRangeBaseSalary(double startSalary, double endSalary, Pageable pageable) {
    auditLogService.save(AuditLog.builder()
      .user(userRepository.findByUserId(1))
      .actionInfo("Filter payrolls by base salary range")
      .actionType("SELECT")
      .build());
    return salaryRecordRepository.findByRangeBaseSalary(startSalary, endSalary, pageable).map(this::convertSalaryRecordToPayrollDTO);
  }

  public Page<PayrollDTO> findByPaymentStatus(String paymentStatus, Pageable pageable) {
    auditLogService.save(AuditLog.builder()
      .user(userRepository.findByUserId(1))
      .actionInfo("Filter payrolls by payment status \"" + paymentStatus + "\"")
      .actionType("SELECT")
      .build());
    return salaryRecordRepository.findByPaymentStatus(paymentStatus, pageable).map(this::convertSalaryRecordToPayrollDTO);
  }

  private PayrollDTO convertSalaryRecordToPayrollDTO(SalaryRecord salaryRecord) {
    return PayrollDTO.builder()
      .employee(salaryRecord.getEmployee())
      .salaryRecord(salaryRecord)
      .build();
  }
}
