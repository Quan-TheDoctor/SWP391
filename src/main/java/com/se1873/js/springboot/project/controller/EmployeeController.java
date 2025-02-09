package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.management.entity.Employee;
import com.se1873.js.springboot.management.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @RequestMapping
  public String index(Model model) {
    model.addAttribute("employees", employeeService.findAllEmployees());
    model.addAttribute("page", "fragments/employee");
    model.addAttribute("header", "fragments/header");
    return "index";
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId,
                     @RequestParam(value = "tab", required = false, defaultValue = "personal") String tab,
                     @RequestParam(value = "edit", required = false) Boolean edit) {

    Employee employee = employeeService.findEmployeeByEmployeeId(employeeId);
    model.addAttribute("employee", employee);
    model.addAttribute("tab", tab);
    model.addAttribute("edit", edit);
    model.addAttribute("page", "fragments/employee-detail");
    model.addAttribute("header", "fragments/header");
    return "index";
  }
}
