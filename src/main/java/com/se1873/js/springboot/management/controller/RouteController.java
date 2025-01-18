package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.enums.DependantRelationshipENUM;
import com.se1873.js.springboot.management.model.Dependant;
import com.se1873.js.springboot.management.model.Employee;
import com.se1873.js.springboot.management.service.DependantService;
import com.se1873.js.springboot.management.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class RouteController {
  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private DependantService dependantService;

  @PostConstruct
  public void init() {
    employeeService.deleteAllEmployees();
    dependantService.deleteAllDependants();

    Employee employee = new Employee();
    employee.setEmployeeFirstName("Anh Quan");
    employee.setEmployeeLastName("Tran");
    employee.setEmployeeEmail("test@hotmail.com");
    employee.setEmployeePhone("0377777504");

    Dependant dependant = new Dependant();
    dependant.setDependantName("Tran Dinh C");
    dependant.setDependantPhone("0913076306");
    dependant.setDependantRelationshipENUM(DependantRelationshipENUM.PARENT);
    dependant.setEmployee(employee);

    List<Dependant> dependants = new ArrayList<>();
    dependants.add(dependant);

    employee.setDependants(dependants);
    dependantService.insertDependant(dependant);
    employeeService.insertEmployee(employee);
  }

  @RequestMapping("/")
  public String index(Model model) {
    model.addAttribute("page", "homepage");
    model.addAttribute("bodyContent", "fragments/homepage");
    return "index";
  }

  @RequestMapping("/employee")
  public String employee(Model model) {
    model.addAttribute("page", "employee");
    model.addAttribute("bodyContent", "fragments/employee");
    return "index";
  }

  @RequestMapping("/attendance")
  public String attendance(Model model) {
    model.addAttribute("page", "attendance");
    model.addAttribute("bodyContent", "fragments/attendance");
    return "index";
  }

  @RequestMapping("/payroll")
  public String payroll(Model model) {
    model.addAttribute("page", "payroll");
    model.addAttribute("bodyContent", "fragments/payroll");
    return "index";
  }
}
