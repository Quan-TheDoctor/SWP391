package com.se1873.js.springboot.project.service.salary_record;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.DepartmentDTOMapper;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.RequestService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.command.SalaryRecordCommandService;
import com.se1873.js.springboot.project.service.salary_record.query.SalaryRecordQueryService;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SalaryRecordService {
  private final DepartmentDTOMapper departmentDTOMapper;

  private final SalaryRecordRepository salaryRecordRepository;
  private final EmployeeRepository employeeRepository;
  private final FinancialPolicyRepository financialPolicyRepository;
  private final DependentRepository dependentRepository;
  private final EmployeeService employeeService;
  private final RequestService requestService;
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final UserService userService;
  private final SalaryRecordQueryService salaryRecordQueryService;
  private final SalaryRecordCommandService salaryRecordCommandService;

  public void deleteSalaryRecord(Integer salaryId) {
    SalaryRecord sr = salaryRecordQueryService.findSalaryRecordBySalaryId(salaryId);

    salaryRecordCommandService.removeSalaryRecord(sr);

  }

  public Page<PayrollDTO> getAllPayrolls(Pageable pageable) {
    var srs = salaryRecordQueryService.getAll(pageable);
    List<PayrollDTO> payrolls = new ArrayList<>();
    for (var salaryRecord : srs) {
      PayrollDTO payroll = payrollDTO(salaryRecord.getSalaryId());
      payrolls.add(payroll);
    }

    return new PageImpl<>(payrolls, pageable, srs.getTotalElements());
  }
  public SalaryRecord findSalaryRecordBySalaryId(Integer salaryId) {
    return salaryRecordQueryService.findSalaryRecordBySalaryId(salaryId);
  }

  //region getAll()
  public Page<PayrollDTO> getAll(Pageable pageable) {
    var salaryRecords = salaryRecordQueryService.getAll(pageable);
    List<PayrollDTO> payrolls = new ArrayList<>();

    for (var salaryRecord : salaryRecords) {
      PayrollDTO payroll = payrollDTO(salaryRecord.getSalaryId());
      payrolls.add(payroll);
    }

    return new PageImpl<>(payrolls, pageable, salaryRecords.getTotalElements());
  }

  public Page<PayrollDTO> filterByMonth(Pageable pageable, Integer month, Integer year, Integer employeeId) {
    Page<SalaryRecord> salaryRecords = salaryRecordRepository.findSalaryRecordsByEmployeeAndMonthAndYear(pageable,month,year,employeeId);
    return salaryRecords.map(salaryRecord -> payrollDTO(salaryRecord.getSalaryId()));
  }

  public Page<PayrollDTO> getPayrollByEmployeeId(Pageable pageable, Integer employeeId){
    Page<SalaryRecord> salaryRecords = salaryRecordRepository.getSalaryRecordsByEmployee_EmployeeIdAndIsDeleted(employeeId, false,pageable);
    return salaryRecords.map(salaryRecord -> payrollDTO(salaryRecord.getSalaryId()));
  }
  public List<TopSalaryDTO> getTop3HighestNetSalary(int month, int year){
    return salaryRecordRepository.findTop3Salaries(month, year);
  }
  public List<AverageSalaryDTO> getAverageSalaryByYear(int year) {

    return salaryRecordRepository.getAverageSalaryByMonthAndDepartment(year);
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
    SalaryRecord salaryRecord = salaryRecordRepository.findSalaryRecordBySalaryIdAndIsDeleted(salaryId, false);
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
    SalaryRecord salaryRecords = salaryRecordRepository.findSalaryRecordBySalaryIdAndIsDeleted(salaryId, false);
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
    DepartmentDTO departmentDTO = departmentDTOMapper.toDTO(departmentRepository.findDepartmentByDepartmentId(form.getSelectedDepartmentId()));
    User user = userService.findUserByUserId(form.getRequesterId());
    EmployeeDTO managerDTO = employeeService.getEmployeeByEmployeeId(departmentDTO.getManagerId());
    Optional<User> approval = userRepository.findUserByEmployee_EmployeeId(managerDTO.getEmployeeId());

    RequestDTO requestDTO = RequestDTO.builder()
      .requesterId(form.getRequesterId())
      .payrollIds(payrollIds)
      .requestDate(LocalDate.now())
      .requestType("Hạch toán lương")
      .requestStatus("pending")
      .approvalName(approval.get().getUsername())
      .build();

    requestService.saveRequest(requestDTO, user, approval.get());
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
  public Resource exportToExcel(List<Integer> payrollIds, LocalDate startDate, LocalDate endDate) {
    List<SalaryRecord> salaryRecords = new ArrayList<>();

    try {
      if (payrollIds != null && !payrollIds.isEmpty()) {
        // Find by ID list
        log.info("Exporting by IDs: {}", payrollIds);
        for (Integer id : payrollIds) {
          SalaryRecord record = salaryRecordRepository.findSalaryRecordBySalaryId(id);
          if (record != null) {
            salaryRecords.add(record);
          } else {
            log.warn("Salary record not found for ID: {}", id);
          }
        }
      } else if (startDate != null && endDate != null) {
        // Find by date range
        log.info("Exporting by date range: {} to {}", startDate, endDate);
        int startMonth = startDate.getMonthValue();
        int startYear = startDate.getYear();
        int endMonth = endDate.getMonthValue();
        int endYear = endDate.getYear();

        // Get all records in the date range
        List<SalaryRecord> allRecords = salaryRecordRepository.findAll();

        salaryRecords = allRecords.stream()
          .filter(record -> {
            int recordYear = record.getYear();
            int recordMonth = record.getMonth();

            int startTotalMonths = startYear * 12 + startMonth;
            int endTotalMonths = endYear * 12 + endMonth;
            int recordTotalMonths = recordYear * 12 + recordMonth;

            return recordTotalMonths >= startTotalMonths && recordTotalMonths <= endTotalMonths;
          })
          .collect(Collectors.toList());

        log.info("Found {} records in date range", salaryRecords.size());
      } else {
        // No filter criteria provided, get all records
        log.info("No filter criteria provided, getting all records");
        salaryRecords = salaryRecordRepository.findAll();
      }

      if (salaryRecords.isEmpty()) {
        log.warn("No salary records found for export");
        // Create an empty workbook with just headers
        return createEmptyExcelFile();
      }

      return createExcelFile(salaryRecords);
    } catch (Exception e) {
      log.error("Error exporting to Excel", e);
      throw new RuntimeException("Error exporting to Excel", e);
    }
  }

  private Resource createEmptyExcelFile() throws IOException {
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Payroll");

      // Create title and header rows
      createTitleAndHeaderRows(workbook, sheet);

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    }
  }

  private Resource createExcelFile(List<SalaryRecord> salaryRecords) throws IOException {
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Payroll");


      createTitleAndHeaderRows(workbook, sheet);


      DataFormat format = workbook.createDataFormat();
      CellStyle currencyStyle = workbook.createCellStyle();
      currencyStyle.setDataFormat(format.getFormat("#,##0.00"));

      // Add data rows
      int rowIdx = 3;
      for (SalaryRecord record : salaryRecords) {
        Row row = sheet.createRow(rowIdx++);

        // Employee name
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName());

        // Month and Year
        row.createCell(1).setCellValue(record.getMonth());
        row.createCell(2).setCellValue(record.getYear());

        // Currency fields with formatting
        Cell cell3 = row.createCell(3);
        cell3.setCellValue(record.getBaseSalary());
        cell3.setCellStyle(currencyStyle);

        Cell cell4 = row.createCell(4);
        cell4.setCellValue(record.getOvertimePay());
        cell4.setCellStyle(currencyStyle);

        Cell cell5 = row.createCell(5);
        cell5.setCellValue(record.getDeductions());
        cell5.setCellStyle(currencyStyle);

        Cell cell6 = row.createCell(6);
        cell6.setCellValue(record.getNetSalary());
        cell6.setCellStyle(currencyStyle);

        // Status
        row.createCell(7).setCellValue(record.getPaymentStatus());
      }

      // Auto-size columns
      for (int i = 0; i < 8; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    }
  }

  private void createTitleAndHeaderRows(Workbook workbook, Sheet sheet) {
    // Title row
    Font titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 16);

    CellStyle titleStyle = workbook.createCellStyle();
    titleStyle.setFont(titleFont);
    titleStyle.setAlignment(HorizontalAlignment.CENTER);

    Row titleRow = sheet.createRow(0);
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue("Payroll Report");
    titleCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

    // Header row
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.WHITE.getIndex());

    CellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setFont(headerFont);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);

    Row headerRow = sheet.createRow(2);
    String[] columns = {"Employee", "Month", "Year", "Base Salary", "Overtime Pay", "Deductions", "Net Salary", "Status"};
    for (int i = 0; i < columns.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(columns[i]);
      cell.setCellStyle(headerStyle);
    }
  }
}
