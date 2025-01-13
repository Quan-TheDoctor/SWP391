package com.se1873.js.springboot.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    // Handle dashboard logic and return the view name
    return "dashboard";
  }

  @GetMapping("/employee")
  public String employee(Model model) {
    // Handle employee logic and return the view name
    return "employee";
  }

  @GetMapping("/attendance")
  public String attendance(Model model) {
    // Handle attendance logic and return the view name
    return "attendance";
  }

  @GetMapping("/form")
  public String form(Model model) {
    // Handle form logic and return the view name
    return "form";
  }

  @GetMapping("/log")
  public String log(Model model) {
    // Handle log logic and return the view name
    return "log";
  }
}