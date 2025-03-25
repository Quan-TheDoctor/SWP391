package com.se1873.js.springboot.project.service.salary_record;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.DepartmentDTOMapper;
import com.se1873.js.springboot.project.mapper.EmployeeDTOMapper;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.RequestService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.command.SalaryRecordCommandService;
import com.se1873.js.springboot.project.service.salary_record.query.SalaryRecordQueryService;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SalaryRecordService {
  private final EmployeeDTOMapper employeeDTOMapper;
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
  public List<TopSalaryDTO> getTop3HighestNetSalary(int month, int year){
    return salaryRecordRepository.findTop3Salaries(month, year);
  }

  public Resource exportToPdf(List<Integer> payrollIds) {
    try {
      File tempFile = File.createTempFile("payroll-details-", ".pdf");

      Document document = new Document(PageSize.A4, 36, 36, 36, 36);
      PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tempFile));
      document.open();

      BaseFont baseFont = BaseFont.createFont("C:\\Users\\Lenovo\\Desktop\\project\\src\\main\\resources\\fonts\\Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

      com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont , 16, com.itextpdf.text.Font.BOLD);
      com.itextpdf.text.Font headingFont = new com.itextpdf.text.Font(baseFont,  14, com.itextpdf.text.Font.BOLD);
      com.itextpdf.text.Font subheadingFont = new com.itextpdf.text.Font(baseFont , 12, com.itextpdf.text.Font.BOLD);
      com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.BOLD);
      com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(baseFont , 10, com.itextpdf.text.Font.BOLD);
      com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(baseFont , 8, com.itextpdf.text.Font.BOLD);
      com.itextpdf.text.Font blueFont = new com.itextpdf.text.Font(baseFont , 10, com.itextpdf.text.Font.BOLD, BaseColor.BLUE);
      com.itextpdf.text.Font redFont = new com.itextpdf.text.Font(baseFont , 10, com.itextpdf.text.Font.BOLD, BaseColor.RED);


      for (Integer payrollId : payrollIds) {
        PayrollDTO payroll = payrollDTO(payrollId);

        if (payroll != null) {
          if (payrollIds.indexOf(payrollId) > 0) {
            document.newPage();
          }

          Paragraph header = new Paragraph("Salary Calculation", titleFont);
          header.setAlignment(Element.ALIGN_CENTER);
          header.setSpacingAfter(20);
          document.add(header);

          Paragraph salaryBreakdownTitle = new Paragraph("Salary Breakdown", headingFont);
          salaryBreakdownTitle.setSpacingAfter(10);
          document.add(salaryBreakdownTitle);

          PdfPTable topTable = new PdfPTable(4);
          topTable.setWidthPercentage(100);
          topTable.setSpacingAfter(15);

          PdfPCell employeeInfoCell = new PdfPCell();
          employeeInfoCell.setColspan(2);
          employeeInfoCell.setPadding(10);
          employeeInfoCell.setBorder(Rectangle.NO_BORDER);

          Paragraph employeeInfo = new Paragraph();
          employeeInfo.add(new Phrase("Employee Name\n", boldFont));
          employeeInfo.add(new Phrase(payroll.getEmployeeLastName() + ", " + payroll.getEmployeeFirstName() + "\n\n", normalFont));

          employeeInfo.add(new Phrase("Date\n", boldFont));
          employeeInfo.add(new Phrase(payroll.getSalaryRecordMonth() + "/" + payroll.getSalaryRecordYear() + "\n\n", normalFont));

          employeeInfo.add(new Phrase("Tax No\n", boldFont));
          employeeInfo.add(new Phrase(payroll.getEmployeeTaxCode(), normalFont));

          employeeInfoCell.addElement(employeeInfo);
          topTable.addCell(employeeInfoCell);

          PdfPCell netSalaryCell = new PdfPCell();
          netSalaryCell.setColspan(2);
          netSalaryCell.setPadding(10);
          netSalaryCell.setBackgroundColor(new BaseColor(239, 246, 255));

          Paragraph netSalaryInfo = new Paragraph();
          netSalaryInfo.add(new Phrase("Net Salary\n", boldFont));
          netSalaryInfo.add(new Phrase("Total Net Salary: ", normalFont));
          netSalaryInfo.add(new Phrase(String.format("%,.0f VND", payroll.getTotalNetSalary()) + "\n", blueFont));
          netSalaryInfo.add(new Phrase("Allowances: ", normalFont));
          netSalaryInfo.add(new Phrase(String.format("%,.0f VND", payroll.getSalaryRecordTotalAllowance()), normalFont));

          netSalaryCell.addElement(netSalaryInfo);
          topTable.addCell(netSalaryCell);

          document.add(topTable);

          PdfPTable detailsTable = new PdfPTable(4);
          detailsTable.setWidthPercentage(100);
          detailsTable.setSpacingAfter(15);

          PdfPCell earningsCell = new PdfPCell();
          earningsCell.setPadding(10);
          earningsCell.setBorder(Rectangle.NO_BORDER);

          Paragraph earnings = new Paragraph();
          earnings.add(new Phrase("Earnings\n", subheadingFont));
          earnings.add(new Phrase("Base Salary: ", normalFont));
          earnings.add(new Phrase(String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()) + "\n", normalFont));
          earnings.add(new Phrase("Overtime: ", normalFont));
          earnings.add(new Phrase(String.format("%,.0f VND", payroll.getSalaryRecordOvertimePay()) + "\n", normalFont));
          earnings.add(new Phrase("Personal Insurance: ", normalFont));
          earnings.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedPersonalInsuranceDeduction()) + "\n", redFont));
          earnings.add(new Phrase("---------------\n", normalFont));
          earnings.add(new Phrase("Total Earnings: ", boldFont));
          double totalEarnings = payroll.getSalaryRecordBaseSalary() + payroll.getSalaryRecordOvertimePay() - payroll.getCalculatedPersonalInsuranceDeduction();
          earnings.add(new Phrase(String.format("%,.0f VND", totalEarnings), blueFont));

          earningsCell.addElement(earnings);
          detailsTable.addCell(earningsCell);

          PdfPCell deductionsCell = new PdfPCell();
          deductionsCell.setPadding(10);
          deductionsCell.setBorder(Rectangle.NO_BORDER);

          Paragraph deductions = new Paragraph();
          deductions.add(new Phrase("Deductions\n", subheadingFont));
          deductions.add(new Phrase("Personal Deduction: ", normalFont));
          deductions.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedPersonalDeduction()) + "\n", normalFont));
          deductions.add(new Phrase("Dependent Deduction: ", normalFont));
          deductions.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedPersonalDependentDeduction()) + "\n", normalFont));
          deductions.add(new Phrase("---------------\n", normalFont));
          deductions.add(new Phrase("---------------\n", normalFont));
          deductions.add(new Phrase("Total Deductions: ", boldFont));
          deductions.add(new Phrase(String.format("%,.0f VND", payroll.getTotalDeductions()), blueFont));

          deductionsCell.addElement(deductions);
          detailsTable.addCell(deductionsCell);

          PdfPCell empContribCell = new PdfPCell();
          empContribCell.setPadding(10);
          empContribCell.setBorder(Rectangle.NO_BORDER);

          Paragraph empContrib = new Paragraph();
          empContrib.add(new Phrase("Employee Contributions\n", subheadingFont));
          empContrib.add(new Phrase("Health Insurance (" + payroll.getCalculatedEmployeeHealthInsuranceAmount() + "%): ", normalFont));
          empContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployeeHealthInsurance()) + "\n", normalFont));
          empContrib.add(new Phrase("Social Insurance (" + payroll.getCalculatedEmployeeSocialInsuranceAmount() + "%): ", normalFont));
          empContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployeeSocialInsurance()) + "\n", normalFont));
          empContrib.add(new Phrase("Union Fee (" + payroll.getCalculatedEmployeeUnionFeeAmount() + "%): ", normalFont));
          empContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployeeUnionFee()) + "\n", normalFont));
          empContrib.add(new Phrase("Unemployment Insurance (" + payroll.getCalculatedEmployeeUnemploymentInsuranceAmount() + "%): ", normalFont));
          empContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployeeUnemploymentInsurance()), normalFont));

          empContribCell.addElement(empContrib);
          detailsTable.addCell(empContribCell);

          PdfPCell emplrContribCell = new PdfPCell();
          emplrContribCell.setPadding(10);
          emplrContribCell.setBorder(Rectangle.NO_BORDER);

          Paragraph emplrContrib = new Paragraph();
          emplrContrib.add(new Phrase("Employer Contributions\n", subheadingFont));
          emplrContrib.add(new Phrase("Health Insurance (" + payroll.getCalculatedEmployerHealthInsuranceAmount() + "%): ", normalFont));
          emplrContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployerHealthInsurance()) + "\n", normalFont));
          emplrContrib.add(new Phrase("Social Insurance (" + payroll.getCalculatedEmployerSocialInsuranceAmount() + "%): ", normalFont));
          emplrContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployerSocialInsurance()) + "\n", normalFont));
          emplrContrib.add(new Phrase("Union Fee (" + payroll.getCalculatedEmployerUnionFeeAmount() + "%): ", normalFont));
          emplrContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployerUnionFee()) + "\n", normalFont));
          emplrContrib.add(new Phrase("Unemployment Insurance (" + payroll.getCalculatedEmployerUnemploymentInsuranceAmount() + "%): ", normalFont));
          emplrContrib.add(new Phrase(String.format("%,.0f VND", payroll.getCalculatedEmployerUnemploymentInsurance()), normalFont));

          emplrContribCell.addElement(emplrContrib);
          detailsTable.addCell(emplrContribCell);

          document.add(detailsTable);

          Paragraph detailedCalcTitle = new Paragraph("Detailed Calculation", headingFont);
          detailedCalcTitle.setSpacingBefore(10);
          detailedCalcTitle.setSpacingAfter(10);
          document.add(detailedCalcTitle);

          PdfPTable calcTable = new PdfPTable(4);
          calcTable.setWidthPercentage(100);
          calcTable.setSpacingAfter(15);

          String[] headers = {"Component", "Rate", "Base Amount", "Amount"};
          for (String headerr : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(headerr, boldFont));
            headerCell.setBackgroundColor(new BaseColor(249, 250, 251));
            headerCell.setPadding(6);
            calcTable.addCell(headerCell);
          }

          addCalcTableRow(calcTable, "Health Insurance (Employee)",
            payroll.getCalculatedEmployeeHealthInsuranceAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployeeHealthInsurance()),
            normalFont);

          addCalcTableRow(calcTable, "Social Insurance (Employee)",
            payroll.getCalculatedEmployeeSocialInsuranceAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployeeSocialInsurance()),
            normalFont);

          addCalcTableRow(calcTable, "Union Fee (Employee)",
            payroll.getCalculatedEmployeeUnionFeeAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployeeUnionFee()),
            normalFont);

          addCalcTableRow(calcTable, "Unemployment Insurance (Employee)",
            payroll.getCalculatedEmployeeUnemploymentInsuranceAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployeeUnemploymentInsurance()),
            normalFont);

          addCalcTableRow(calcTable, "Health Insurance (Employer)",
            payroll.getCalculatedEmployerHealthInsuranceAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployerHealthInsurance()),
            normalFont);

          addCalcTableRow(calcTable, "Social Insurance (Employer)",
            payroll.getCalculatedEmployerSocialInsuranceAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployerSocialInsurance()),
            normalFont);

          addCalcTableRow(calcTable, "Union Fee (Employer)",
            payroll.getCalculatedEmployerUnionFeeAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployerUnionFee()),
            normalFont);

          addCalcTableRow(calcTable, "Unemployment Insurance (Employer)",
            payroll.getCalculatedEmployerUnemploymentInsuranceAmount() + "%",
            String.format("%,.0f VND", payroll.getSalaryRecordBaseSalary()),
            String.format("%,.0f VND", payroll.getCalculatedEmployerUnemploymentInsurance()),
            normalFont);

          document.add(calcTable);

          Paragraph footer = new Paragraph("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), smallFont);
          footer.setAlignment(Element.ALIGN_RIGHT);
          footer.setSpacingBefore(20);
          document.add(footer);
        }
      }

      document.close();

      return new FileSystemResource(tempFile);
    } catch (Exception e) {
      log.error("Error generating PDF: " + e.getMessage(), e);
      throw new RuntimeException("Failed to generate PDF", e);
    }
  }

  private void addCalcTableRow(PdfPTable table, String component, String rate, String baseAmount, String amount, com.itextpdf.text.Font font) {
    table.addCell(new PdfPCell(new Phrase(component, font)));
    table.addCell(new PdfPCell(new Phrase(rate, font)));
    table.addCell(new PdfPCell(new Phrase(baseAmount, font)));
    table.addCell(new PdfPCell(new Phrase(amount, font)));
  }

  public Map<Integer, Double> getMonthlySalaryData() {
    Map<Integer, Double> monthlySalaryData = new HashMap<>();
    List<Object[]> results = salaryRecordRepository.getMonthlyNetSalaryData();
    
    for (Object[] row : results) {
      Integer month = (Integer) row[0];
      Double total = (Double) row[1];
      monthlySalaryData.put(month, total);
    }
    
    return monthlySalaryData;
  }

  public Map<Integer, Double> getMonthlyDeductionsData() {
    Map<Integer, Double> monthlyDeductionsData = new HashMap<>();
    List<Object[]> results = salaryRecordRepository.getMonthlyDeductionsData();
    
    for (Object[] row : results) {
      Integer month = (Integer) row[0];
      Double total = (Double) row[1];
      monthlyDeductionsData.put(month, total);
    }
    
    return monthlyDeductionsData;
  }

  public double getTotalNetSalary() {
    Double total = salaryRecordRepository.getTotalNetSalary();
    return total != null ? total : 0.0;
  }

  public double getTotalUnpaidSalary() {
    Double total = salaryRecordRepository.getTotalUnpaidSalary();
    return total != null ? total : 0.0;
  }

  public double getTotalCompanyTaxContributions() {
    Double total = salaryRecordRepository.getTotalCompanyTaxContributions();
    return total != null ? total : 0.0;
  }

  private List<PayrollDTO> getAllPayrollsByYear(int year) {
    Page<PayrollDTO> allPayrolls = getAllPayrolls(PageRequest.of(0, Integer.MAX_VALUE));

    return allPayrolls.getContent().stream()
      .filter(p -> p.getSalaryRecordYear() == year)
      .collect(Collectors.toList());
  }


  public Page<PayrollDTO> getSortedPayrolls(Pageable pageable, String field, String direction) {
    Page<PayrollDTO> allPayrolls = getAllPayrolls(pageable);
    List<PayrollDTO> payrollList = new ArrayList<>(allPayrolls.getContent());

    if (field == null || field.isEmpty()) {
      return allPayrolls;
    }

    boolean isAscending = "asc".equalsIgnoreCase(direction);

    String[] fields = field.split(",");

    payrollList.sort((p1, p2) -> {
      int result = 0;

      for (String singleField : fields) {
        result = compareByField(p1, p2, singleField.trim());

        if (result != 0) {
          break;
        }
      }

      return isAscending ? result : -result;
    });

    return new PageImpl<>(
      payrollList,
      pageable,
      allPayrolls.getTotalElements()
    );
  }

  private int compareByField(PayrollDTO p1, PayrollDTO p2, String field) {
    switch (field) {
      case "employee.firstName":
        return compareStrings(p1.getEmployeeFirstName(), p2.getEmployeeFirstName());

      case "baseSalary":
        return compareDoubles(p1.getSalaryRecordBaseSalary(), p2.getSalaryRecordBaseSalary());

      case "netSalary":
        return compareDoubles(p1.getSalaryRecordNetSalary(), p2.getSalaryRecordNetSalary());

      case "deductions":
        return compareDoubles(p1.getSalaryRecordDeductions(), p2.getSalaryRecordDeductions());

      case "insuranceDeduction":
        return compareDoubles(p1.getSalaryRecordInsuranceDeduction(), p2.getSalaryRecordInsuranceDeduction());

      case "month":
        return compareIntegers(p1.getSalaryRecordMonth(), p2.getSalaryRecordMonth());

      case "year":
        return compareIntegers(p1.getSalaryRecordYear(), p2.getSalaryRecordYear());

      case "paymentStatus":
        return compareStrings(p1.getSalaryRecordPaymentStatus(), p2.getSalaryRecordPaymentStatus());

      default:
        return 0;
    }
  }

  private int compareStrings(String s1, String s2) {
    if (s1 == null && s2 == null) return 0;
    if (s1 == null) return -1;
    if (s2 == null) return 1;
    return s1.compareTo(s2);
  }

  private int compareIntegers(Integer i1, Integer i2) {
    if (i1 == null && i2 == null) return 0;
    if (i1 == null) return -1;
    if (i2 == null) return 1;
    return i1.compareTo(i2);
  }

  private int compareDoubles(Double d1, Double d2) {
    if (d1 == null && d2 == null) {
      return 0;
    } else if (d1 == null) {
      return -1;
    } else if (d2 == null) {
      return 1;
    }
    return Double.compare(d1, d2);
  }

  public List<AverageSalaryDTO> getAverageSalaryByYear(int year) {
    return salaryRecordRepository.getAverageSalaryByYear(year);
  }
  public Resource exportToExcel(List<Integer> payrollIds, LocalDate startDate, LocalDate endDate) {
    List<SalaryRecord> salaryRecords = new ArrayList<>();

    try {
      if (payrollIds != null && !payrollIds.isEmpty()) {
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
        log.info("Exporting by date range: {} to {}", startDate, endDate);
        int startMonth = startDate.getMonthValue();
        int startYear = startDate.getYear();
        int endMonth = endDate.getMonthValue();
        int endYear = endDate.getYear();
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
        log.info("No filter criteria provided, getting all records");
        salaryRecords = salaryRecordRepository.findAll();
      }

      if (salaryRecords.isEmpty()) {
        log.warn("No salary records found for export");
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

      int rowIdx = 3;
      for (SalaryRecord record : salaryRecords) {
        Row row = sheet.createRow(rowIdx++);

        Cell cell0 = row.createCell(0);
        cell0.setCellValue(record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName());

        row.createCell(1).setCellValue(record.getMonth());
        row.createCell(2).setCellValue(record.getYear());

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

        row.createCell(7).setCellValue(record.getPaymentStatus());
      }

      for (int i = 0; i < 8; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    }
  }

  private void createTitleAndHeaderRows(Workbook workbook, Sheet sheet) {
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
  public void deleteSalaryRecord(Integer salaryId) {
    SalaryRecord sr = salaryRecordQueryService.findSalaryRecordBySalaryId(salaryId);

    salaryRecordCommandService.removeSalaryRecord(sr);

  }

  @Cacheable(value = "allPayrolls")
  public Page<PayrollDTO> getAllPayrolls(Pageable pageable) {
    Page<SalaryRecord> salaryRecords = salaryRecordRepository.findSalaryRecordsByIsDeleted(false, pageable);
    List<PayrollDTO> payrolls = salaryRecords.getContent().stream()
      .map(salaryRecord -> payrollDTO(salaryRecord.getSalaryId()))
      .collect(Collectors.toList());

    return new PageImpl<>(payrolls, pageable, salaryRecords.getTotalElements());
  }
  public PayrollDTO findSalaryRecordBySalaryId(Integer salaryId) {
    return payrollDTO(salaryId);
  }

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
    EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employee.getEmployeeId());
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
      .departmentName(employeeDTO.getDepartmentName())
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
  public void savePayroll(PayrollCalculationForm form) {
    List<Integer> payrollIds = new ArrayList<>();
    int workingDaysPerMonth = (int) financialPolicyRepository.getFinancialPolicyAmount(12);
    double lateDayPenalty = financialPolicyRepository.getFinancialPolicyAmount(13);
    for (var payroll : form.getPayrollCalculations()) {
      EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(payroll.getEmployeeId());

      SalaryCalculationResult calculated = calculateSalaryComponents(
        employeeDTO.getContractBaseSalary(),
        payroll.getWorkedDays(),
        payroll.getLateDays(),
        payroll.getOvertimeHours(),
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
      .employee(employeeRepository.findEmployeeByEmployeeId(payroll.getEmployeeId()))
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
      .isDeleted(false)
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

  public double calculateTotalCompanyTaxContribution(List<PayrollDTO> payrolls) {
    double totalContribution = 0.0;
    
    for (PayrollDTO payroll : payrolls) {
      if ("Paid".equals(payroll.getSalaryRecordPaymentStatus())) {
        double employerHealthInsurance = payroll.getCalculatedEmployerHealthInsurance() != null ?
                                         payroll.getCalculatedEmployerHealthInsurance() : 0.0;
        double employerSocialInsurance = payroll.getCalculatedEmployerSocialInsurance() != null ? 
                                        payroll.getCalculatedEmployerSocialInsurance() : 0.0;
        double employerUnionFee = payroll.getCalculatedEmployerUnionFee() != null ? 
                                  payroll.getCalculatedEmployerUnionFee() : 0.0;
        double employerUnemploymentInsurance = payroll.getCalculatedEmployerUnemploymentInsurance() != null ? 
                                              payroll.getCalculatedEmployerUnemploymentInsurance() : 0.0;
        
        totalContribution += employerHealthInsurance + employerSocialInsurance + 
                             employerUnionFee + employerUnemploymentInsurance;
      }
    }
    
    return totalContribution;
  }

  private record SalaryCalculationResult(
    double dailyRate,
    double proratedBaseSalary,
    double latePenaltyDeduction,
    double overtimePay,
    double grossSalary
  ) {
  }
}
