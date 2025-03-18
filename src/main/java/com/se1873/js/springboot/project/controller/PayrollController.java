package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.FinancialPolicyDTOList;
import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.service.FinancialPolicyService;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @RequestMapping
  public String payroll(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var payrolls = salaryRecordService.getAll(pageable);
    Double totalNetSalary = payrolls.getContent().stream().map(PayrollDTO::getSalaryRecordNetSalary).mapToDouble(Double::doubleValue).sum();
    Double unpaidSalary = payrolls.getContent().stream().filter(payrollDTO -> Boolean.parseBoolean(payrollDTO.getSalaryRecordPaymentStatus())).map(PayrollDTO::getSalaryRecordNetSalary).mapToDouble(Double::doubleValue).sum();

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

  @RequestMapping("/policies/save")
  public String policiesSave(Model model,
                             @RequestParam(value = "service", required = false) String service,
                             @ModelAttribute("financialPolicyDTOList") FinancialPolicyDTOList financialPolicyDTOList,
                             BindingResult bindingResult) {
    if ("cancel".equals(service)) {
      return "redirect:/payroll";
    }

    if (bindingResult.hasErrors()) {
      model.addAttribute("contentFragment", "fragments/financial-policies-fragments");
      return "index";
    }
    if ("save".equals(service)) {
      financialPolicyService.saveAll(financialPolicyDTOList.getFinancialPolicies());
      model.addAttribute("message", "Update successfully");
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

    // Fetch the payrolls for the current page
    var payrolls = salaryRecordService.getAll(pageable);

    // Calculate total net salary and unpaid salary
    Double totalNetSalary = payrolls.getContent().stream()
            .map(PayrollDTO::getSalaryRecordNetSalary)
            .mapToDouble(Double::doubleValue).sum();
    Double unpaidSalary = payrolls.getContent().stream()
            .filter(payrollDTO -> Boolean.parseBoolean(payrollDTO.getSalaryRecordPaymentStatus()))
            .map(PayrollDTO::getSalaryRecordNetSalary)
            .mapToDouble(Double::doubleValue).sum();

    // Pass data to model
    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("contentFragment", "fragments/payroll-export-fragments");

    // Add pagination details
    model.addAttribute("currentPage", page + 1);  // Convert to 1-based page number
    model.addAttribute("totalPages", payrolls.getTotalPages());
    model.addAttribute("totalEmployees", payrolls.getTotalElements()); // Total records

    return "index";  // Use the main view template
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
      // Đặt ngày là ngày cuối cùng của tháng
      YearMonth yearMonth = YearMonth.parse(endDateStr);
      endDate = yearMonth.atEndOfMonth();
    }

    Resource file = salaryRecordService.exportToExcel(payrollIds, startDate, endDate);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
  }

}
