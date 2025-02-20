package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.FinancialPolicyDTOList;
import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.service.FinancialPolicyService;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll")
public class PayrollController {


  private final SalaryRecordService salaryRecordService;
  private final FinancialPolicyService financialPolicyService;

  @RequestMapping
  public String payroll(Model model) {
    var payrolls = salaryRecordService.getAll(PageRequest.of(0, 10));
    Double totalNetSalary = payrolls.getContent().stream().map(PayrollDTO::getSalaryRecordNetSalary).mapToDouble(Double::doubleValue).sum();
    Double unpaidSalary = payrolls.getContent().stream().filter(payrollDTO -> Boolean.parseBoolean(payrollDTO.getSalaryRecordPaymentStatus())).map(PayrollDTO::getSalaryRecordNetSalary).mapToDouble(Double::doubleValue).sum();

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    return "payroll";
  }

  @RequestMapping("/policies")
  public String policies(Model model) {
    var policies = financialPolicyService.getAll();
    FinancialPolicyDTOList financialPolicyDTOList = new FinancialPolicyDTOList();
    financialPolicyDTOList.setFinancialPolicies(policies);
    model.addAttribute("financialPolicyDTOList", financialPolicyDTOList);
    return "policies";
  }

  @RequestMapping("/policies/save")
  public String policiesSave(Model model,
                             @RequestParam(value = "service", required = false) String service,
                             @ModelAttribute("financialPolicyDTOList") FinancialPolicyDTOList financialPolicyDTOList,
                             BindingResult bindingResult) {
    if("cancel".equals(service)) {
      return "redirect:/payroll";
    }

    if(bindingResult.hasErrors()) {
      return "policies";
    }
    if("save".equals(service)) {
      financialPolicyService.saveAll(financialPolicyDTOList.getFinancialPolicies());
      model.addAttribute("message", "Update successfully");
    }
    return "policies";
  }

  @GetMapping("/payroll/detail/{id}")
  public String getPayrollDetail(@PathVariable("id") int id, Model model) {
    PayrollDTO payroll = salaryRecordService.getPayrollDetailBySalaryId(id);
    model.addAttribute("payroll", payroll);
    return "payroll-details";
  }
}
