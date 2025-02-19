package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SalaryRecordService {

  private final SalaryRecordRepository salaryRecordRepository;
  private final EmployeeRepository employeeRepository;

  public Page<PayrollDTO> getAll(Pageable pageable) {
    var salaryRecords = salaryRecordRepository.findAll(pageable);
    List<PayrollDTO> payrolls = new ArrayList<>();

    for(var salaryRecord : salaryRecords) {
      Employee employee = salaryRecord.getEmployee();
      PayrollDTO payroll = convertSalaryRecordToPayrollDTO(salaryRecord, employee);
      payrolls.add(payroll);
    }

    return new PageImpl<>(payrolls, pageable, salaryRecords.getTotalElements());
  }

  private PayrollDTO convertSalaryRecordToPayrollDTO(SalaryRecord salaryRecord, Employee employee) {
    return PayrollDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeFirstName(employee.getFirstName())
      .employeeLastName(employee.getLastName())
      .salaryRecordId(salaryRecord.getSalaryId())
      .salaryRecordMonth(salaryRecord.getMonth())
      .salaryRecordYear(salaryRecord.getYear())
      .salaryRecordBaseSalary(salaryRecord.getBaseSalary())
      .salaryRecordTotalAllowance(salaryRecord.getTotalAllowance())
      .salaryRecordOvertimePay(salaryRecord.getOvertimePay())
      .salaryRecordDeductions(salaryRecord.getDeductions())
      .salaryRecordInsuranceDeduction(salaryRecord.getInsuranceDeduction())
      .salaryRecordTaxAmount(salaryRecord.getTaxAmount())
      .salaryRecordNetSalary(salaryRecord.getNetSalary())
      .salaryRecordPaymentStatus(salaryRecord.getPaymentStatus())
      .build();
  }
}
