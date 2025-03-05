package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SalaryRecordService {

  private final SalaryRecordRepository salaryRecordRepository;
  private final EmployeeRepository employeeRepository;
  private final FinancialPolicyRepository financialPolicyRepository;
  private final DependentRepository dependentRepository;
  private final EmployeeService employeeService;
  private final RequestService requestService;
  private final UserRepository userRepository;

  public SalaryRecord findSalaryRecordBySalaryId(Integer salaryId) {
    return salaryRecordRepository.findSalaryRecordBySalaryId(salaryId);
  }

  //region getAll()
  public Page<PayrollDTO> getAll(Pageable pageable) {
    var salaryRecords = salaryRecordRepository.findAll(pageable);
    List<PayrollDTO> payrolls = new ArrayList<>();

    for (var salaryRecord : salaryRecords) {
      PayrollDTO payroll = payrollDTO(salaryRecord.getSalaryId());
      payrolls.add(payroll);
    }

    return new PageImpl<>(payrolls, pageable, salaryRecords.getTotalElements());
  }

  //endregion
  public Map<Integer, Double> getMonthlySalary(int year) {
    List<Object[]> results = salaryRecordRepository.getTotalSalaryByMonth(year);
    Map<Integer, Double> payrollMap = new HashMap<>();

    for (int i = 1; i <= 12; i++) {
      payrollMap.put(i, 0.0);
    }

    for (Object[] row : results) {
      Integer month = (Integer) row[0];
      Double total = (Double) row[1];
      payrollMap.put(month, total);
    }
    return payrollMap;
  }
  //region payrollDTO()
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
    double personalDeduction = financialPolicyRepository.getFinancialPolicyAmount(10);
    return personalDeduction + calculatedPersonalDependentDeduction(salaryId);
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

    double grossSalary = totalEarning - totalDeductions;
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
      .totalDeductions(totalDeductions(salaryId))
      .totalTaxAmount(taxAmount)
      .totalNetSalary(totalNetSalary)
      .totalGrossSalary(grossSalary)
      .build();
  }
  //endregion

  //region SavePayroll()
  public void savePayroll(PayrollCalculationForm form) {
    List<Integer> payrollIds = new ArrayList<>();
    int workingDaysPerMonth = (int) financialPolicyRepository.getFinancialPolicyAmount(12);
    double lateDayPenalty = financialPolicyRepository.getFinancialPolicyAmount(13);
    for (var payroll : form.getPayrollCalculations()) {
      EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(payroll.employeeId());

      SalaryCalculationResult calculated = calculateSalaryComponents(
        employeeDTO.getContractBaseSalary(),
        payroll.workedDays(),
        payroll.lateDays(),
        payroll.overtimeHours(),
        workingDaysPerMonth,
        lateDayPenalty
      );

      SalaryRecord salaryRecord = buildInitialSalaryRecord(payroll, employeeDTO, calculated);
      salaryRecord = salaryRecordRepository.save(salaryRecord);

      updateSalaryRecordWithCalculations(salaryRecord);

      payrollIds.add(salaryRecord.getSalaryId());
    }
    createRequest(form, payrollIds);
  }

  private void createRequest(PayrollCalculationForm form, List<Integer> payrollIds) {
    Optional<User> user = userRepository.findUserByUserId(form.getRequesterId());
    Optional<User> approval = userRepository.findUserByUsername("annguyen");

    RequestDTO requestDTO = RequestDTO.builder()
      .requesterId(form.getRequesterId())
      .payrollIds(payrollIds)
      .requestDate(LocalDate.now())
      .requestType("Hạch toán lương")
      .requestStatus("pending")
      .approvalName(approval.get().getUsername())
      .build();

    requestService.saveRequest(requestDTO, user.get(), approval.get());
  }

  private SalaryCalculationResult calculateSalaryComponents(
    double contractBaseSalary,
    int workedDays,
    int lateDays,
    double overtimeHours,
    int workingDaysPerMonth,
    double lateDayPenalty
  ) {
    double dailyRate = contractBaseSalary / workingDaysPerMonth;
    double proratedBaseSalary = workedDays * dailyRate;
    double latePenaltyDeduction = lateDays * lateDayPenalty;
    double overtimePay = calculateOvertimePay(overtimeHours, dailyRate);

    double grossSalary = proratedBaseSalary + overtimePay - latePenaltyDeduction;

    return new SalaryCalculationResult(
      dailyRate,
      proratedBaseSalary,
      latePenaltyDeduction,
      overtimePay,
      grossSalary
    );
  }

  private double calculateOvertimePay(double overtimeHours, double dailyRate) {
    int standardWorkingHoursPerDay = (int) financialPolicyRepository.getFinancialPolicyAmount(14);
    double hourlyRate = dailyRate / standardWorkingHoursPerDay;
    return overtimeHours * hourlyRate * financialPolicyRepository.getFinancialPolicyAmount(9);
  }

  private SalaryRecord buildInitialSalaryRecord(
    PayrollCalculationDTO payroll,
    EmployeeDTO employeeDTO,
    SalaryCalculationResult calculated
  ) {
    return SalaryRecord.builder()
      .employee(employeeRepository.findEmployeeByEmployeeId(payroll.employeeId()))
      .baseSalary(calculated.proratedBaseSalary())
      .month(LocalDate.now().getMonthValue())
      .year(LocalDate.now().getYear())
      .overtimePay(calculated.overtimePay())
      .totalAllowance(0.0)
      .deductions(calculated.latePenaltyDeduction())
      .insuranceDeduction(0.0)
      .taxAmount(0.0)
      .netSalary(0.0)
      .paymentStatus("Pending")
      .build();
  }

  private void updateSalaryRecordWithCalculations(SalaryRecord salaryRecord) {
    PayrollDTO payrollDTO = payrollDTO(salaryRecord.getSalaryId());

    salaryRecord.setTotalAllowance(payrollDTO.getSalaryRecordTotalAllowance());
    salaryRecord.setDeductions(payrollDTO.getSalaryRecordDeductions());
    salaryRecord.setInsuranceDeduction(payrollDTO.getSalaryRecordInsuranceDeduction());
    salaryRecord.setTaxAmount(payrollDTO.getSalaryRecordTaxAmount());
    salaryRecord.setNetSalary(payrollDTO.getSalaryRecordNetSalary());

    salaryRecordRepository.save(salaryRecord);
  }

  private record SalaryCalculationResult(
    double dailyRate,
    double proratedBaseSalary,
    double latePenaltyDeduction,
    double overtimePay,
    double grossSalary
  ) {
  }
  //endregion

  /**
   * @param salaryId: Input salary ID for manipulating the desired salary
   * @return PayrollDTO
   * @deprecated
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
