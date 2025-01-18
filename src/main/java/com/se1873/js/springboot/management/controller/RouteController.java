package com.se1873.js.springboot.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class RouteController {
  private String bodyContent = "bodyContent";
  private final String index = index;
  
  @RequestMapping("/")
  public String index(Model model) {

    model.addAttribute("page", "homepage");
    model.addAttribute(bodyContent, "fragments/homepage");
    return index;
  }

  @RequestMapping("/employee")
  public String employee(Model model) {
    model.addAttribute("page", "employee");
    model.addAttribute(bodyContent, "fragments/employee");
    return index;
  }

  @RequestMapping("/attendance")
  public String attendance(Model model) {
    model.addAttribute("page", "attendance");
    model.addAttribute(bodyContent, "fragments/attendance");
    return index;
  }

  @RequestMapping("/payroll")
  public String payroll(Model model) {
    model.addAttribute("page", "payroll");
    model.addAttribute(bodyContent, "fragments/payroll");
    return index;
  }
}
