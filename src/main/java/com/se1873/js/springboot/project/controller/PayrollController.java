package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.FinancialPolicyDTOList;
import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.service.FinancialPolicyService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import com.se1873.js.springboot.project.service.salary_record.query.SalaryRecordQueryService;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll")
public class PayrollController {

  private final SalaryRecordService salaryRecordService;
  private final FinancialPolicyService financialPolicyService;
  private final SalaryRecordQueryService salaryRecordQueryService;
  private final EmployeeService employeeService;

  @RequestMapping
  public String payroll(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var payrolls = salaryRecordService.getAll(pageable);
    Double totalNetSalary = payrolls.getContent().stream()
      .filter(payrollDTO -> payrollDTO.getSalaryRecordPaymentStatus().equals("Paid"))
      .map(PayrollDTO::getSalaryRecordNetSalary).mapToDouble(Double::doubleValue).sum();
    Double unpaidSalary = payrolls.getContent()
      .stream()
      .filter(payrollDTO -> payrollDTO.getSalaryRecordPaymentStatus().equals("Pending")).map(PayrollDTO::getSalaryRecordNetSalary).mapToDouble(Double::doubleValue).sum();

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("contentFragment", "fragments/payroll-fragments");
    return "index";
  }

  @RequestMapping("/policies")
  public String policies(Model model) {
    var policies = financialPolicyService.getAll();
    FinancialPolicyDTOList financialPolicyDTOList = new FinancialPolicyDTOList();
    financialPolicyDTOList.setFinancialPolicies(policies);
    model.addAttribute("financialPolicyDTOList", financialPolicyDTOList);
    model.addAttribute("contentFragment", "fragments/financial-policies-fragments");
    return "index";
  }

  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("query") String query) {
    log.info(query);
    var employees = employeeService.search(PageRequest.of(1, 1000), query);
    List<PayrollDTO> payrolls = new ArrayList<>();

    log.info(employees.getContent().toString());

    for(var e : employees) {
      Page<PayrollDTO> initialPayrolls = salaryRecordService.getPayrollByEmployeeId(PageRequest.of(0, 10), e.getEmployeeId());

      payrolls.addAll(initialPayrolls.getContent());
    }


    model.addAttribute("payrolls", new PageImpl<>(payrolls, PageRequest.of(1, 10), payrolls.size()));
    model.addAttribute("contentFragment", "fragments/payroll-fragments");
    return "index";
  }

  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam(value = "field", required = false) String[] field,
                       @RequestParam(value = "value", required = false) String[] value) {
    Page<PayrollDTO> initialPayrolls = salaryRecordService.getAllPayrolls(PageRequest.of(0, 10));

    List<PayrollDTO> filteredPayrolls = salaryRecordQueryService.filterPayrolls(
      initialPayrolls.getContent(), field, null, value
    );

    log.info(filteredPayrolls.toString());

    Page<PayrollDTO> resultPage = new PageImpl<>(
      filteredPayrolls,
      PageRequest.of(1, 10),
      initialPayrolls.getTotalElements()
    );

    double totalNetSalary = salaryRecordQueryService.calculateTotalNetSalary(filteredPayrolls);
    double unpaidSalary = salaryRecordQueryService.calculateUnpaidSalary(filteredPayrolls);

    updateModel(model, resultPage, totalNetSalary, unpaidSalary);

    return "index";
  }

  private void updateModel(Model model, Page<PayrollDTO> payrolls, double totalNetSalary, double unpaidSalary) {
    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("contentFragment", "fragments/payroll-fragments");
  }

  @RequestMapping("/policies/save")
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
  public String getPayrollDetail(@RequestParam("salaryId") Integer id, Model model) {
    PayrollDTO payroll = salaryRecordService.payrollDTO(id);
    model.addAttribute("payroll", payroll);
    model.addAttribute("contentFragment", "fragments/payroll-details-fragments");
    return "index";
  }

  @GetMapping("/slip")
  public String getPayrollSlip(@RequestParam("salaryId") Integer id, Model model) {
    PayrollDTO payroll = salaryRecordService.payrollDTO(id);
    model.addAttribute("payroll", payroll);
    model.addAttribute("contentFragment", "fragments/payroll-slip-fragments");
    return "index";
  }

  @RequestMapping("/export/view")
  public String exportView(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);

    var payrolls = salaryRecordService.getAll(pageable);

    Double totalNetSalary = payrolls.getContent().stream()
      .map(PayrollDTO::getSalaryRecordNetSalary)
      .mapToDouble(Double::doubleValue).sum();
    Double unpaidSalary = payrolls.getContent().stream()
      .filter(payrollDTO -> payrollDTO.getSalaryRecordPaymentStatus().equals("Pending"))
      .map(PayrollDTO::getSalaryRecordNetSalary)
      .mapToDouble(Double::doubleValue).sum();

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("contentFragment", "fragments/payroll-export-fragments");

    model.addAttribute("currentPage", page + 1);
    model.addAttribute("totalPages", payrolls.getTotalPages());
    model.addAttribute("totalEmployees", payrolls.getTotalElements());

    return "index";
  }

  @RequestMapping("/export")
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
  public String deletePayroll(@RequestParam("salaryId") Integer id, Model model) {
    try {
      salaryRecordService.deleteSalaryRecord(id);
      model.addAttribute("message", "Delete successfully");
    } catch (Exception e) {
      log.error(e.getMessage());
      model.addAttribute("message", "Delete failed");
      model.addAttribute("messageType", "error");
    }
    return "redirect:/payroll";
  }

  @PostMapping("/bulk-delete")
  public String bulkDeletePayroll(@RequestParam("selectedIds") String selectedIds, Model model) {
    try {
      List<Integer> ids = Arrays.stream(selectedIds.split(","))
        .map(Integer::parseInt)
        .collect(Collectors.toList());

//      salaryRecordService.bulkDeleteSalaryRecords(ids);
      model.addAttribute("message", "Selected records deleted successfully");
    } catch (Exception e) {
      log.error("Bulk delete failed: " + e.getMessage());
      model.addAttribute("message", "Failed to delete selected records");
      model.addAttribute("messageType", "error");
    }
    return "redirect:/payroll";
  }

  @GetMapping("/export-pdf")
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
  public String sortPayrolls(
    @RequestParam(value = "field", required = false) String field,
    @RequestParam(value = "direction", defaultValue = "asc") String direction,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "5") int size,
    Model model) {

    Pageable pageable = PageRequest.of(page, size);
    Page<PayrollDTO> payrolls = salaryRecordService.getSortedPayrolls(pageable, field, direction);

    Double totalNetSalary = payrolls.getContent().stream()
      .filter(payrollDTO -> payrollDTO.getSalaryRecordPaymentStatus().equals("Paid"))
      .map(PayrollDTO::getSalaryRecordNetSalary)
      .mapToDouble(Double::doubleValue).sum();

    Double unpaidSalary = payrolls.getContent().stream()
      .filter(payrollDTO -> payrollDTO.getSalaryRecordPaymentStatus().equals("Pending"))
      .map(PayrollDTO::getSalaryRecordNetSalary)
      .mapToDouble(Double::doubleValue).sum();

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("sortField", field);
    model.addAttribute("direction", direction);
    model.addAttribute("contentFragment", "fragments/payroll-fragments");

    return "index";
  }
}
