package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.Employee;
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
import java.util.Optional;

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


  public double calculateInsuranceOrFee(int salaryId, int financialPolicyId) {
    SalaryRecord salaryRecord = salaryRecordRepository.findSalaryRecordBySalaryId(salaryId);
    double policyRate = financialPolicyRepository.getFinancialPolicyAmount(financialPolicyId);

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
      + calculateInsuranceOrFee(salaryId, 5)
      + calculateInsuranceOrFee(salaryId, 7);
  }

  public double calculatedPersonalDependentDeduction(int salaryId) {
    int numberOfDependents = dependentRepository.getNumberOfDependentsBySalaryID(salaryId);
    double policyRate = financialPolicyRepository.getFinancialPolicyAmount(11);
    return numberOfDependents * policyRate;
  }

  public double insuranceDeduction(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 1)
      + calculateInsuranceOrFee(salaryId, 2)
      + calculateInsuranceOrFee(salaryId, 3)
      + calculateInsuranceOrFee(salaryId, 4)
      + calculateInsuranceOrFee(salaryId, 5)
      + calculateInsuranceOrFee(salaryId, 6)
      + calculateInsuranceOrFee(salaryId, 7)
      + calculateInsuranceOrFee(salaryId, 8);
  }

  public double totalDeductions(int salaryId) {
    return calculateInsuranceOrFee(salaryId, 10)
      + calculatedPersonalDependentDeduction(salaryId);

  }

  public double taxAmount(double totalEarning, double totalDeduction) {
    double salaryAfterDeductions = totalEarning - totalDeduction;

    if (salaryAfterDeductions <= 0) {
      return 0;
    } else if (0 < salaryAfterDeductions && salaryAfterDeductions <= 5000000) {
      return salaryAfterDeductions * 0.05;
    } else if (5000000 < salaryAfterDeductions && salaryAfterDeductions <= 10000000) {
      return salaryAfterDeductions * 0.05 + (salaryAfterDeductions - 10000000) * 0.1;
    } else if (10000000 < salaryAfterDeductions && salaryAfterDeductions <= 20000000) {
      return salaryAfterDeductions * 0.05 + 5000000 * 0.1 + (salaryAfterDeductions - 10000000) * 0.15;
    } else if (18000000 < salaryAfterDeductions && salaryAfterDeductions <= 30000000) {
      return salaryAfterDeductions * 0.05 + 5000000 * 0.1 + 10000000 * 0.15 + (salaryAfterDeductions - 10000000) * 0.2;
    } else {
      return salaryAfterDeductions * 0.05 + 5000000 * 0.1 + 10000000 * 0.15 + 10000000 * 0.2 + (salaryAfterDeductions - 10000000) * 0.25;
    }
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
    double totalDeductions = calculatedPersonalDependentDeduction + calculatedPersonalDeduction;
    double totalEarning = salaryRecords.getBaseSalary() + salaryRecords.getOvertimePay() - calculatedPersonalInsuranceDeduction;
    double taxAmount = taxAmount(totalEarning, totalDeductions);
    double totalNetSalary = totalEarning - taxAmount + salaryRecords.getTotalAllowance();

    return PayrollDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeFirstName(employee.getFirstName())
      .employeeLastName(employee.getLastName())
      .employeeTaxCode(employee.getTaxCode())
      .salaryRecordId(salaryRecords.getSalaryId())
      .salaryRecordMonth(salaryRecords.getMonth())
      .salaryRecordYear(salaryRecords.getYear())
      .salaryRecordBaseSalary(salaryRecords.getBaseSalary())
      .salaryRecordTotalAllowance(salaryRecords.getTotalAllowance())
      .salaryRecordOvertimePay(salaryRecords.getOvertimePay())
      .salaryRecordDeductions(totalDeductions(salaryId))
      .salaryRecordTaxAmount(taxAmount)
      .salaryRecordInsuranceDeduction(insuranceDeduction(salaryId))
      .salaryRecordNetSalary(totalNetSalary)
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
      .totalTaxAmount(taxAmount)
      .totalNetSalary(totalNetSalary)
      .build();
  }

  /**
   * @deprecated
   * @param salaryId: Input salary ID for manipulating the desired salary
   * @return
   */
  @Deprecated(since = "2025/02/22", forRemoval = true)
  public PayrollDTO getInsurance(int salaryId) {
    SalaryRecord salaryRecords = salaryRecordRepository.findSalaryRecordBySalaryId(salaryId);
    double netsalary = salaryRecords.getNetSalary();
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

    return PayrollDTO.builder()
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
      .totalNetSalary(netsalary)

      .build();
  }
}
