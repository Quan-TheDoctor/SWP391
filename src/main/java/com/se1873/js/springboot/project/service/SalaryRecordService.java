package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.FinancialPolicy;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.DependentRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.FinancialPolicyRepository;
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
  private final FinancialPolicyRepository financialPolicyRepository;
  private final DependentRepository dependentRepository;

  public Page<PayrollDTO> getAll(Pageable pageable) {
    var salaryRecords = salaryRecordRepository.findAll(pageable);
    List<PayrollDTO> payrolls = new ArrayList<>();

    for (var salaryRecord : salaryRecords) {
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


  public PayrollDTO getPayrollDetailBySalaryId(int salaryId) {
    SalaryRecord salaryRecords = salaryRecordRepository.findSalaryRecordBySalaryId(salaryId);
    Employee employee = employeeRepository.findEmployeeByEmployeeId(salaryRecords.getEmployee().getEmployeeId());
    List<FinancialPolicy> financialPolicy = financialPolicyRepository.findAll();

    return PayrollDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeFirstName(employee.getFirstName())
      .employeeLastName(employee.getLastName())
      .salaryRecordId(salaryRecords.getSalaryId())
      .salaryRecordMonth(salaryRecords.getMonth())
      .salaryRecordYear(salaryRecords.getYear())
      .salaryRecordBaseSalary(salaryRecords.getBaseSalary())
      .salaryRecordTotalAllowance(salaryRecords.getTotalAllowance())
      .salaryRecordOvertimePay(salaryRecords.getOvertimePay())
      .salaryRecordDeductions(salaryRecords.getDeductions())
      .salaryRecordTaxAmount(salaryRecords.getTaxAmount())
      .salaryRecordInsuranceDeduction(salaryRecords.getInsuranceDeduction())
      .salaryRecordNetSalary(salaryRecords.getNetSalary())
      .salaryRecordPaymentStatus(salaryRecords.getPaymentStatus())
      .build();
  }


  public double calculateInsuranceOrFee(int salaryId, int financialPolicyId) {
    SalaryRecord salaryRecord = salaryRecordRepository.findSalaryRecordBySalaryId(salaryId);
    double policyRate = financialPolicyRepository.getFinancialPolicyAmount(financialPolicyId);

    log.info(salaryRecord.getBaseSalary() * policyRate / 100 + "");
    return salaryRecord.getBaseSalary() * policyRate / 100;
  }

  public double calculatedEmployeeSocialInsurance(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 3);
  }

  public double calculatedEmployeeUnionFee(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 5);
  }

  public double calculatedEmployeeHealthInsurance(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 1);
  }

  public double calculatedEmployeeUnemploymentInsurance(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 7);
  }

  public double calculatedEmployerHealthInsurance(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 2);
  }

  public double calculatedEmployerSocialInsurance(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 4);
  }

  public double calculatedEmployerUnionFee(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 6);
  }

  public double calculatedEmployerUnemploymentInsurance(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 8);
  }

  public double calculatedPersonalInsuranceDeduction(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 1)
      + calculateInsuranceOrFee(salaryId, 3)
      + calculateInsuranceOrFee(salaryId, 5);
  }

  public double calculatedPersonalDependentDeduction(int salaryId) {
    int numberofDependance = dependentRepository.getNumberOfDependentsBySalaryID(salaryId);
    double policyRate = financialPolicyRepository.getFinancialPolicyAmount(11);
    double dependentDedcution = numberofDependance * policyRate;
    return dependentDedcution;
  }

  public double totalDeductions(int salaryId) {
    return calculatedEmployeeSocialInsurance(salaryId)
      + calculateInsuranceOrFee(salaryId, 10)
      + calculatedPersonalDependentDeduction(salaryId);
  }

  public PayrollDTO payrollDTO(int salaryId) {
    SalaryRecord salaryRecords = salaryRecordRepository.findSalaryRecordBySalaryId(salaryId);
    Employee employee = employeeRepository.findEmployeeByEmployeeId(salaryRecords.getEmployee().getEmployeeId());

    double calculatedEmployeeHealthInsuranceAmount = financialPolicyRepository.getFinancialPolicyAmount(1);
    double calculatedEmployeeSocialInsuranceAmount = financialPolicyRepository.getFinancialPolicyAmount(3);
    double calculatedEmployeeUnionFeeAmount = financialPolicyRepository.getFinancialPolicyAmount(5);
    double calculatedEmployeeUnemploymentInsuranceAmount = financialPolicyRepository.getFinancialPolicyAmount(7);
    double calculatedEmployerHealthInsuranceAmount = financialPolicyRepository.getFinancialPolicyAmount(2);
    double calculatedEmployerSocialInsuranceAmount = financialPolicyRepository.getFinancialPolicyAmount(4);
    double calculatedEmployerUnionFeeAmount = financialPolicyRepository.getFinancialPolicyAmount(6);
    double calculatedEmployerUnemploymentInsuranceAmount = financialPolicyRepository.getFinancialPolicyAmount(8);


    double calculatedEmployeeHealthInsurance = calculatedEmployeeHealthInsurance(salaryId);
    double calculatedEmployeeSocialInsurance = calculatedEmployeeSocialInsurance(salaryId);
    double calculatedEmployeeUnionFee = calculatedEmployeeUnionFee(salaryId);
    double calculatedEmployeeUnemploymentInsurance = calculatedEmployeeUnemploymentInsurance(salaryId);

    double calculatedEmployerHealthInsurance = calculatedEmployerHealthInsurance(salaryId);
    double calculatedEmployerSocialInsurance = calculatedEmployerSocialInsurance(salaryId);
    double calculatedEmployerUnionFee = calculatedEmployerUnionFee(salaryId);
    double calculatedEmployerUnemploymentInsurance = calculatedEmployerUnemploymentInsurance(salaryId);

    double calculatedPersonalInsuranceDeduction = calculatedPersonalInsuranceDeduction(salaryId);
    double calculatedPersonalDeduction = financialPolicyRepository.getFinancialPolicyAmount(10);
    double calculatedPersonalDependentDeduction = calculatedPersonalDependentDeduction(salaryId);
//    double totalDeductions = totalDeductions(salaryId);
    double totalDeductions = calculatedPersonalInsuranceDeduction + calculatedPersonalDependentDeduction + calculatedPersonalDeduction;

    double baseSalary = salaryRecords.getBaseSalary();
    double totalOTEarning = salaryRecords.getOvertimePay();
    double totalEarning = baseSalary + totalOTEarning;
    double totalTaxAmount = 0;
    if(totalEarning > totalDeductions) totalTaxAmount = 0.1 * (totalEarning - totalDeductions);

    double totalNetSalary = totalEarning - calculatedEmployeeHealthInsurance - calculatedEmployeeSocialInsurance - calculatedEmployeeUnionFee - calculatedEmployeeUnemploymentInsurance - totalTaxAmount ;


    return PayrollDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeFirstName(employee.getFirstName())
      .employeeLastName(employee.getLastName())
      .salaryRecordId(salaryRecords.getSalaryId())
      .salaryRecordMonth(salaryRecords.getMonth())
      .salaryRecordYear(salaryRecords.getYear())
      .salaryRecordBaseSalary(salaryRecords.getBaseSalary())
      .salaryRecordTotalAllowance(salaryRecords.getTotalAllowance())
      .salaryRecordOvertimePay(salaryRecords.getOvertimePay())
      .salaryRecordDeductions(salaryRecords.getDeductions())
      .salaryRecordTaxAmount(salaryRecords.getTaxAmount())
      .salaryRecordInsuranceDeduction(salaryRecords.getInsuranceDeduction())
      .salaryRecordNetSalary(salaryRecords.getNetSalary())
      .salaryRecordPaymentStatus(salaryRecords.getPaymentStatus())
      .calculatedEmployeeHealthInsurance(calculatedEmployeeHealthInsurance)
      .calculatedEmployeeHealthInsuranceAmount(calculatedEmployeeHealthInsuranceAmount)
      .calculatedEmployeeSocialInsurance(calculatedEmployeeSocialInsurance)
      .calculatedEmployeeSocialInsuranceAmount(calculatedEmployeeSocialInsuranceAmount)
      .calculatedEmployeeUnionFee(calculatedEmployeeUnionFee)
      .calculatedEmployeeUnionFeeAmount(calculatedEmployeeUnionFeeAmount)
      .calculatedEmployeeUnemploymentInsurance(calculatedEmployeeUnemploymentInsurance)
      .calculatedEmployeeUnemploymentInsuranceAmount(calculatedEmployeeUnemploymentInsuranceAmount)
      .calculatedEmployerHealthInsurance(calculatedEmployerHealthInsurance)
      .calculatedEmployerHealthInsuranceAmount(calculatedEmployerHealthInsuranceAmount)
      .calculatedEmployerSocialInsurance(calculatedEmployerSocialInsurance)
      .calculatedEmployerSocialInsuranceAmount(calculatedEmployerSocialInsuranceAmount)
      .calculatedEmployerUnionFee(calculatedEmployerUnionFee)
      .calculatedEmployerUnionFeeAmount(calculatedEmployerUnionFeeAmount)
      .calculatedEmployerUnemploymentInsurance(calculatedEmployerUnemploymentInsurance)
      .calculatedEmployerUnemploymentInsuranceAmount(calculatedEmployerUnemploymentInsuranceAmount)
      .calculatedPersonalInsuranceDeduction(calculatedPersonalInsuranceDeduction)
      .calculatedPersonalDeduction(calculatedPersonalDeduction)
      .calculatedPersonalDependentDeduction(calculatedPersonalDependentDeduction)
      .totalDeductions(totalDeductions)
      .totalTaxAmount(totalTaxAmount)
      .totalNetSalary(totalNetSalary)
      .build();
  }

}
