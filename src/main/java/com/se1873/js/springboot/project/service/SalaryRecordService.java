package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.comparator.Comparators;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryRecordService {

  private final SalaryRecordRepository salaryRecordRepository;
  private final AuditLogService auditLogService;
  private final UserRepository userRepository;

  public List<PayrollDTO> findAlls() {
    return salaryRecordRepository.findAll().stream().map(this::convertSalaryRecordToPayrollDTO).toList();
  }

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

  public List<PayrollDTO> sortByField(String field, String direction, List<PayrollDTO> payrollDTOS) {
    if ("paymentStatus".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getPaymentStatus)
              .orElse("")
              .toLowerCase(),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    } else if ("baseSalary".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getBaseSalary)
              .orElse(null),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    } else if ("netSalary".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getNetSalary)
              .orElse(null),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    } else if ("employee.firstName".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getEmployee())
              .map(Employee::getFirstName)
              .orElse("")
              .toLowerCase(),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    } else if ("employee.lastName".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getEmployee())
              .map(Employee::getLastName)
              .orElse("")
              .toLowerCase(),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    } else if ("month,year".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getMonth)
              .orElse(null),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getYear)
              .orElse(null),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    } else if ("deductions,insuranceDeduction".equals(field)) {
      payrollDTOS = payrollDTOS.stream()
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getDeductions)
              .orElse(null),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .sorted(Comparator.comparing(
          (PayrollDTO p) ->
            Optional.ofNullable(p.getSalaryRecord())
              .map(SalaryRecord::getInsuranceDeduction)
              .orElse(null),
          direction.equals("asc") ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
    }
    return payrollDTOS;
  }
}
