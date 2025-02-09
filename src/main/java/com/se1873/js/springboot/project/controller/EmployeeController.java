package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;
  private Set<Employee> employees;
  private Set<EmployeeDTO> employeeDTOs;
  @RequestMapping
  public String index(Model model,
                      @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
                      @RequestParam(value = "field", required = false, defaultValue = "employeeCode") String field) {
    employeeDTOs = employeeService.findAllEmployees();

    model.addAttribute("employees", employeeDTOs);

    model.addAttribute("field", field);
    model.addAttribute("direction", direction);
    model.addAttribute("page", "fragments/employee");
    model.addAttribute("header", "fragments/header");
    return "index";
  }

  @RequestMapping("/sort")
  public String sort(Model model,
                      @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
                      @RequestParam(value = "field", required = false, defaultValue = "employeeCode") String field) {
    model.addAttribute("employees", employeeService.sortEmployeeSByFieldAndDirection(field, direction));
    model.addAttribute("field", field);
    model.addAttribute("direction", direction);
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
    EmploymentHistory currentEmployementHistory = employee.getEmploymentHistories().stream()
        .filter(EmploymentHistory::getIsCurrent)
          .findFirst()
            .orElse(null);

    model.addAttribute("employee", employee);
    model.addAttribute("currentEmployementHistory", currentEmployementHistory);
    model.addAttribute("tab", tab);
    model.addAttribute("edit", edit);
    model.addAttribute("page", "fragments/employee-detail");
    model.addAttribute("header", "fragments/header");
    return "index";
  }

  @RequestMapping("/update")
  public String update(@ModelAttribute("employee") Employee employee) {

    for(var his : employee.getEmploymentHistories()) {
      System.out.println(his);
    }

    return "index";
  }
}
