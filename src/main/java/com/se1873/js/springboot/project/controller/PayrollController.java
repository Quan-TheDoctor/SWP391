package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import com.se1873.js.springboot.project.service.FinancialPolicyService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import com.se1873.js.springboot.project.service.salary_record.query.SalaryRecordQueryService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.position.PositionService;
import com.se1873.js.springboot.project.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll")
public class PayrollController {

  private final SalaryRecordService salaryRecordService;
  private final SalaryRecordQueryService salaryRecordQueryService;
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final PositionService positionService;
  private final FinancialPolicyService financialPolicyService;
  private final GlobalController globalController;
  private final EmailUtils emailUtils;
  private final TemplateEngine templateEngine;

  @GetMapping
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String payroll(Model model, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PayrollDTO> payrolls = salaryRecordService.getAllPayrolls(pageable);

    double totalNetSalary = salaryRecordService.getTotalNetSalary();
    double unpaidSalary = salaryRecordService.getTotalUnpaidSalary();
    double companyTaxContributions = salaryRecordService.getTotalCompanyTaxContributions();

    Map<Integer, Double> monthlySalaryData = salaryRecordService.getMonthlySalaryData();
    Map<Integer, Double> monthlyDeductionsData = salaryRecordService.getMonthlyDeductionsData();

    List<String> labels = new ArrayList<>();
    List<Double> salaryValues = new ArrayList<>();
    List<Double> deductionValues = new ArrayList<>();

    for (int i = 1; i <= 12; i++) {
      salaryValues.add(monthlySalaryData.getOrDefault(i, 0.0));
      deductionValues.add(monthlyDeductionsData.getOrDefault(i, 0.0));
    }

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("companyTaxContributions", companyTaxContributions);
    model.addAttribute("labels", labels);
    model.addAttribute("salaryValues", salaryValues);
    model.addAttribute("deductionValues", deductionValues);
    model.addAttribute("contentFragment", "fragments/payroll-fragments");
    return "index";
  }

  @RequestMapping("/policies")
  @PreAuthorize("hasPermission('PAYROLL', 'MANAGE')")
  public String policies(Model model) {
    var policies = financialPolicyService.getAll();
    FinancialPolicyDTOList financialPolicyDTOList = new FinancialPolicyDTOList();
    financialPolicyDTOList.setFinancialPolicies(policies);
    model.addAttribute("financialPolicyDTOList", financialPolicyDTOList);
    model.addAttribute("contentFragment", "fragments/financial-policies-fragments");
    return "index";
  }

  @RequestMapping("/search")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String search(Model model,
                       @RequestParam("query") String query) {
    var employees = employeeService.search(PageRequest.of(1, 1000), query);
    List<PayrollDTO> payrolls = new ArrayList<>();

    for(var e : employees) {
      Page<PayrollDTO> initialPayrolls = salaryRecordService.getPayrollByEmployeeId(PageRequest.of(0, 10), e.getEmployeeId());

      payrolls.addAll(initialPayrolls.getContent());
    }

    double totalNetSalary = salaryRecordQueryService.calculateTotalNetSalary(payrolls);
    double unpaidSalary = salaryRecordQueryService.calculateUnpaidSalary(payrolls);
    double companyTaxContributions = salaryRecordService.calculateTotalCompanyTaxContribution(payrolls);
    
    ChartDataDTO chartData = generateChartDataFromPayrolls(payrolls);
    model.addAttribute("chartData", chartData);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("companyTaxContributions", companyTaxContributions);

    model.addAttribute("payrolls", new PageImpl<>(payrolls, PageRequest.of(1, 10), payrolls.size()));
    model.addAttribute("contentFragment", "fragments/payroll-fragments");
    return "index";
  }

  @RequestMapping("/filter")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String filter(Model model,
                       @RequestParam(value = "field", required = false) String[] field,
                       @RequestParam(value = "value", required = false) String[] value) {
    Page<PayrollDTO> initialPayrolls = salaryRecordService.getAllPayrolls(PageRequest.of(0, 10));

    List<PayrollDTO> filteredPayrolls = salaryRecordQueryService.filterPayrolls(
      initialPayrolls.getContent(), field, null, value
    );

    Page<PayrollDTO> resultPage = new PageImpl<>(
      filteredPayrolls,
      PageRequest.of(1, 10),
      initialPayrolls.getTotalElements()
    );

    double totalNetSalary = salaryRecordQueryService.calculateTotalNetSalary(filteredPayrolls);
    double unpaidSalary = salaryRecordQueryService.calculateUnpaidSalary(filteredPayrolls);
    double companyTaxContributions = salaryRecordService.calculateTotalCompanyTaxContribution(filteredPayrolls);

    ChartDataDTO chartData = generateChartDataFromPayrolls(filteredPayrolls);
    model.addAttribute("chartData", chartData);
    model.addAttribute("companyTaxContributions", companyTaxContributions);

    updateModel(model, resultPage, totalNetSalary, unpaidSalary);

    return "index";
  }
  @GetMapping("/multi-filter")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String multiFilter(
    Model model,
    @RequestParam(value = "year", required = false) String year,
    @RequestParam(value = "month", required = false) String month,
    @RequestParam(value = "salaryRange", required = false) String salaryRange,
    @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
    @RequestParam(value = "department", required = false) String department,
    @RequestParam(value = "position", required = false) String position,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    boolean isAllFilters = (year == null || year.equals("all")) &&
                          (month == null || month.equals("all")) &&
                          (salaryRange == null || salaryRange.equals("all")) &&
                          (paymentStatus == null || paymentStatus.equals("all")) &&
                          (department == null || department.equals("all")) &&
                          (position == null || position.equals("all"));

    Page<PayrollDTO> initialPayrolls;
    if (isAllFilters) {
      initialPayrolls = salaryRecordService.getAllPayrolls(PageRequest.of(0, Integer.MAX_VALUE));
    } else {
      initialPayrolls = salaryRecordService.getAllPayrolls(PageRequest.of(0, Integer.MAX_VALUE));
    }

    List<PayrollDTO> filteredPayrolls = new ArrayList<>(initialPayrolls.getContent());

    if (year != null && !year.equals("all")) {
      filteredPayrolls = filteredPayrolls.stream()
        .filter(p -> p.getSalaryRecordYear() == Integer.parseInt(year))
        .collect(Collectors.toList());
    }

    if (month != null && !month.equals("all")) {
      filteredPayrolls = filteredPayrolls.stream()
        .filter(p -> p.getSalaryRecordMonth() == Integer.parseInt(month))
        .collect(Collectors.toList());
    }

    if (salaryRange != null && !salaryRange.equals("all")) {
      String[] range = salaryRange.split(",");
      double min = Double.parseDouble(range[0]);
      double max = Double.parseDouble(range[1]);

      filteredPayrolls = filteredPayrolls.stream()
        .filter(p -> p.getSalaryRecordNetSalary() >= min && p.getSalaryRecordNetSalary() <= max)
        .collect(Collectors.toList());
    }

    if (paymentStatus != null && !paymentStatus.equals("all")) {
      filteredPayrolls = filteredPayrolls.stream()
        .filter(p -> p.getSalaryRecordPaymentStatus().equals(paymentStatus))
        .collect(Collectors.toList());
    }

    if (department != null && !department.equals("all")) {
      try {
        Integer departmentId = Integer.parseInt(department);
        filteredPayrolls = filteredPayrolls.stream()
          .filter(p -> {
            EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(p.getEmployeeId());
            return employee != null && employee.getDepartmentId() != null
              && employee.getDepartmentId().equals(departmentId);
          })
          .collect(Collectors.toList());
      } catch (NumberFormatException e) {
        log.warn("Invalid department ID format: {}", department);
      }
    }

    if (position != null && !position.equals("all")) {
      try {
        Integer positionId = Integer.parseInt(position);
        filteredPayrolls = filteredPayrolls.stream()
          .filter(p -> {
            EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(p.getEmployeeId());
            return employee != null && employee.getPositionId() != null
              && employee.getPositionId().equals(positionId);
          })
          .collect(Collectors.toList());
      } catch (NumberFormatException e) {
        log.warn("Invalid position ID format: {}", position);
      }
    }

    int start = page * size;
    int end = Math.min(start + size, filteredPayrolls.size());
    List<PayrollDTO> pageContent = filteredPayrolls.subList(start, end);
    
    Page<PayrollDTO> resultPage = new PageImpl<>(
      pageContent,
      PageRequest.of(page, size),
      filteredPayrolls.size()
    );

    double totalNetSalary = salaryRecordQueryService.calculateTotalNetSalary(filteredPayrolls);
    double unpaidSalary = salaryRecordQueryService.calculateUnpaidSalary(filteredPayrolls);
    double companyTaxContributions = salaryRecordService.calculateTotalCompanyTaxContribution(filteredPayrolls);
    ChartDataDTO chartData = generateChartDataFromPayrolls(filteredPayrolls);

    updateModel(model, resultPage, totalNetSalary, unpaidSalary);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("companyTaxContributions", companyTaxContributions);
    model.addAttribute("currentMonth", new SimpleDateFormat("MMMM").format(new Date()));
    model.addAttribute("chartData", chartData);
    model.addAttribute("departments", departmentService.getAllDepartments());
    model.addAttribute("positions", positionService.getAllPositions());
    return "index";
  }


  private ChartDataDTO generateChartDataFromPayrolls(List<PayrollDTO> payrolls) {
    Map<Integer, Map<String, Object>> monthsData = new HashMap<>();
    
    for (PayrollDTO payroll : payrolls) {
      Integer month = payroll.getSalaryRecordMonth();
      double netSalary = payroll.getSalaryRecordNetSalary();
      double deductions = payroll.getSalaryRecordDeductions() + payroll.getSalaryRecordInsuranceDeduction() + payroll.getSalaryRecordTaxAmount();
      
      monthsData.computeIfAbsent(month, k -> new HashMap<>());
      
      Map<String, Object> monthData = monthsData.get(month);
      double currentNetSalary = (double) monthData.getOrDefault("netSalary", 0.0);
      double currentDeductions = (double) monthData.getOrDefault("deductions", 0.0);
      
      monthData.put("netSalary", currentNetSalary + netSalary);
      monthData.put("deductions", currentDeductions + deductions);
    }
    
    Map<Integer, Double> netSalaryMap = new HashMap<>();
    Map<Integer, Double> deductionsMap = new HashMap<>();
    
    for (Map.Entry<Integer, Map<String, Object>> entry : monthsData.entrySet()) {
      Integer month = entry.getKey();
      Map<String, Object> monthData = entry.getValue();
      
      netSalaryMap.put(month, (double) monthData.getOrDefault("netSalary", 0.0));
      deductionsMap.put(month, (double) monthData.getOrDefault("deductions", 0.0));
    }
    
    return new ChartDataDTO(netSalaryMap, deductionsMap);
  }

  private void updateModel(Model model, Page<PayrollDTO> payrolls, double totalNetSalary, double unpaidSalary) {
    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    double companyTaxContributions = salaryRecordService.calculateTotalCompanyTaxContribution(payrolls.getContent());
    model.addAttribute("companyTaxContributions", companyTaxContributions);
    model.addAttribute("contentFragment", "fragments/payroll-fragments");
  }

  @RequestMapping("/policies/save")
  @PreAuthorize("hasPermission('PAYROLL', 'MANAGE')")
  public String policiesSave(Model model,
                             @RequestParam(value = "service", required = false) String service,
                             @ModelAttribute("financialPolicyDTOList") FinancialPolicyDTOList financialPolicyDTOList,
                             BindingResult bindingResult) {
    if ("cancel".equals(service)) {
      return "redirect:/payroll";
    }

    if (bindingResult.hasErrors()) {
      model.addAttribute("message", "Setting configs failed.");
      model.addAttribute("messageType", "error");
      model.addAttribute("contentFragment", "fragments/financial-policies-fragments");
      return "index";
    }
    if ("save".equals(service)) {
      financialPolicyService.saveAll(financialPolicyDTOList.getFinancialPolicies());
      model.addAttribute("message", "Setting configured successfully");
    }
    model.addAttribute("contentFragment", "fragments/financial-policies-fragments");
    return "index";
  }

  @GetMapping("/detail")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String viewPayrollDetail(@RequestParam("salaryId") Integer salaryId, Model model) {
    PayrollDTO payroll = salaryRecordService.findSalaryRecordBySalaryId(salaryId);

    if (payroll == null) {
      return "redirect:/payroll?error=Payroll+record+not+found";
    }

    model.addAttribute("payroll", payroll);
    model.addAttribute("contentFragment", "fragments/payroll-details-fragments");
    return "index";
  }

  @GetMapping("/slip")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String getPayrollSlip(@RequestParam("salaryId") Integer id, Model model) {
    PayrollDTO payroll = salaryRecordService.payrollDTO(id);
    model.addAttribute("payroll", payroll);
    model.addAttribute("contentFragment", "fragments/payroll-slip-fragments");
    return "index";
  }

  @RequestMapping("/export/view")
  @PreAuthorize("hasPermission('PAYROLL', 'UPDATE')")
  public String exportView(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);

    var payrolls = salaryRecordService.getAll(pageable);

    Double totalNetSalary = payrolls.getContent().stream()
      .map(PayrollDTO::getSalaryRecordNetSalary)
      .mapToDouble(Double::doubleValue).sum();
    Double unpaidSalary = payrolls.getContent().stream()
      .filter(payrollDTO -> payrollDTO.getSalaryRecordPaymentStatus().equals("Pending"))
      .map(PayrollDTO::getSalaryRecordNetSalary)
      .mapToDouble(Double::doubleValue).sum();
    Double companyTaxContributions = salaryRecordService.calculateTotalCompanyTaxContribution(payrolls.getContent());

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("companyTaxContributions", companyTaxContributions);
    model.addAttribute("contentFragment", "fragments/payroll-export-fragments");

    model.addAttribute("currentPage", page + 1);
    model.addAttribute("totalPages", payrolls.getTotalPages());
    model.addAttribute("totalEmployees", payrolls.getTotalElements());

    return "index";
  }

  @RequestMapping("/export")
  @PreAuthorize("hasPermission('PAYROLL', 'UPDATE')")
  public ResponseEntity<Resource> exportPayroll(
    @RequestParam(value = "selectedPayrolls", required = false) String selectedPayrolls,
    @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM") String startDateStr,
    @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM") String endDateStr) {

    List<Integer> payrollIds = null;
    if (selectedPayrolls != null && !selectedPayrolls.isEmpty()) {
      payrollIds = Arrays.stream(selectedPayrolls.split(","))
        .filter(s -> !s.isEmpty())
        .map(Integer::parseInt)
        .collect(Collectors.toList());
    }
    LocalDate startDate = null;
    LocalDate endDate = null;

    if (startDateStr != null && !startDateStr.isEmpty()) {
      startDate = LocalDate.parse(startDateStr + "-01");
    }

    if (endDateStr != null && !endDateStr.isEmpty()) {
      YearMonth yearMonth = YearMonth.parse(endDateStr);
      endDate = yearMonth.atEndOfMonth();
    }

    Resource file = salaryRecordService.exportToExcel(payrollIds, startDate, endDate);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll.xlsx")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(file);
  }

  @GetMapping("/delete")
  @PreAuthorize("hasPermission('PAYROLL', 'MANAGE')")
  public String deletePayroll(@RequestParam("salaryId") Integer salaryId) {
    try {
      salaryRecordService.deleteSalaryRecord(salaryId);
      return "redirect:/payroll?success=Payroll+record+deleted+successfully";
    } catch (Exception e) {
      log.error("Error deleting payroll record: {}", e.getMessage());
      return "redirect:/payroll?error=Failed+to+delete+payroll+record";
    }
  }

  @PostMapping("/bulk-delete")
  @PreAuthorize("hasPermission('PAYROLL', 'MANAGE')")
  public String bulkDeletePayrolls(@RequestParam("selectedIds") String selectedIdsStr) {
    try {
      String[] selectedIdsArray = selectedIdsStr.split(",");
      List<Integer> selectedIds = Arrays.stream(selectedIdsArray)
        .map(Integer::parseInt)
        .collect(Collectors.toList());

      for (Integer id : selectedIds) {
        salaryRecordService.deleteSalaryRecord(id);
      }

      return "redirect:/payroll?success=Selected+payroll+records+deleted+successfully";
    } catch (Exception e) {
      log.error("Error bulk deleting payroll records: {}", e.getMessage());
      return "redirect:/payroll?error=Failed+to+delete+selected+payroll+records";
    }
  }

  @GetMapping("/export-pdf")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public ResponseEntity<Resource> exportPdf(@RequestParam("ids") String ids) {
    List<Integer> payrollIds = Arrays.stream(ids.split(","))
      .map(Integer::parseInt)
      .collect(Collectors.toList());

    Resource pdfFile = salaryRecordService.exportToPdf(payrollIds);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-slips.pdf")
      .contentType(MediaType.APPLICATION_PDF)
      .body(pdfFile);
  }

  @GetMapping("/sort")
  @PreAuthorize("hasPermission('PAYROLL', 'VISIBLE')")
  public String sort(Model model,
                     @RequestParam("field") String field,
                     @RequestParam(value = "direction", defaultValue = "asc") String direction,
                     @RequestParam(value = "page", defaultValue = "0") int page,
                     @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);

    String[] fields = field.split(",");

    Page<PayrollDTO> payrolls = salaryRecordService.getAllPayrolls(pageable);
    List<PayrollDTO> sortedPayrolls = new ArrayList<>(payrolls.getContent());

    if (direction.equals("asc")) {
      if (fields.length > 1) {
        sortedPayrolls.sort((p1, p2) -> {
          int result = compareField(p1, p2, fields[0]);
          if (result == 0 && fields.length > 1) {
            return compareField(p1, p2, fields[1]);
          }
          return result;
        });
      } else {
        sortedPayrolls.sort((p1, p2) -> compareField(p1, p2, field));
      }
    } else {
      if (fields.length > 1) {
        sortedPayrolls.sort((p1, p2) -> {
          int result = compareField(p2, p1, fields[0]);
          if (result == 0 && fields.length > 1) {
            return compareField(p2, p1, fields[1]);
          }
          return result;
        });
      } else {
        sortedPayrolls.sort((p1, p2) -> compareField(p2, p1, field));
      }
    }

    Page<PayrollDTO> resultPage = new PageImpl<>(
      sortedPayrolls,
      pageable,
      payrolls.getTotalElements()
    );

    double totalNetSalary = salaryRecordQueryService.calculateTotalNetSalary(sortedPayrolls);
    double unpaidSalary = salaryRecordQueryService.calculateUnpaidSalary(sortedPayrolls);

    ChartDataDTO chartData = generateChartDataFromPayrolls(sortedPayrolls);
    model.addAttribute("chartData", chartData);
    model.addAttribute("sortField", field);
    model.addAttribute("direction", direction);

    updateModel(model, resultPage, totalNetSalary, unpaidSalary);

    return "index";
  }

  private int compareField(PayrollDTO p1, PayrollDTO p2, String field) {
    switch (field) {
      case "employeeId":
        return p1.getEmployeeId().compareTo(p2.getEmployeeId());
      case "basicSalary":
        return Double.compare(p1.getSalaryRecordBaseSalary(), p2.getSalaryRecordBaseSalary());
      case "netSalary":
        return Double.compare(p1.getSalaryRecordNetSalary(), p2.getSalaryRecordNetSalary());
      case "month":
        return Integer.compare(p1.getSalaryRecordMonth(), p2.getSalaryRecordMonth());
      case "year":
        return Integer.compare(p1.getSalaryRecordYear(), p2.getSalaryRecordYear());
      case "paymentStatus":
        return p1.getSalaryRecordPaymentStatus().compareTo(p2.getSalaryRecordPaymentStatus());
      default:
        return 0;
    }
  }

  @PostMapping("/send-payslip")
  @PreAuthorize("hasPermission('PAYROLL', 'MANAGE')")
  public String sendPayrollSlip(@RequestParam("salaryId") Integer salaryId, Model model) {
    try {
      PayrollDTO payroll = salaryRecordService.payrollDTO(salaryId);
      String month = String.valueOf(payroll.getSalaryRecordMonth());
      String year = String.valueOf(payroll.getSalaryRecordYear());
      String employeeName = payroll.getEmployeeFirstName() + " " + payroll.getEmployeeLastName();
      String employeeEmail = employeeService.getEmployeeByEmployeeId(payroll.getEmployeeId()).getEmployeePersonalEmail();

      Context context = new Context();
      context.setVariable("payroll", payroll);
      String payslipContent = templateEngine.process("fragments/payroll-slip-fragments", context);

      emailUtils.sendPayslipEmail(employeeEmail, employeeName, month, year, payslipContent);

      return "redirect:/payroll/detail?salaryId=" + salaryId + "&success=Payslip+sent+successfully";
    } catch (Exception e) {
      log.error("Error sending payslip: ", e);
      return "redirect:/payroll/detail?salaryId=" + salaryId + "&error=Failed+to+send+payslip";
    }
  }

}
