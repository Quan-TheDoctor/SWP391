package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.service.DepartmentService;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final PositionService positionService;

  @RequestMapping
  public String employee(Model model) {
    List<EmployeeDTO> employees = employeeService.getAllEmployees();
    model.addAttribute("employees", employees);
    model.addAttribute("page", "fragments/employee");
    model.addAttribute("header", "fragments/header");
    return "index";
  }

  @RequestMapping("/view")
  public String viewEmployee(Model model,
                             @RequestParam(value = "id", required = false, defaultValue = "null") Integer employeeId,
                             @RequestParam(value = "edit", required = false, defaultValue = "null") Boolean edit,
                             @RequestParam(value = "tab", required = false, defaultValue = "personal") String tab) {
    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(employeeId);
    if(employee == null) {
      employee = new EmployeeDTO();
    }
    List<Department> departments = departmentService.getAllDepartments();
    List<Position> positions = positionService.getAllPositions();
    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("edit", edit);
    model.addAttribute("employee", employee);
    model.addAttribute("tab", tab);
    model.addAttribute("page", "fragments/employee-detail");
    model.addAttribute("header", "fragments/header");
    return "index";
  }

  @RequestMapping("/update")
  public String updateEmployee(@ModelAttribute("employee") EmployeeDTO employee) {
    for (var de : employee.getDependents()) {
      System.out.println(de.getFullName());
    }
//    employeeService.updateEmployee(employee);
    return "redirect:/employee";
  }

  @RequestMapping("/create")
  public String createEmployee(Model model,
                               @RequestParam(value = "tab", required = false, defaultValue = "personal") String tab) {
    model.addAttribute("employee", new EmployeeDTO());
    model.addAttribute("tab", tab);
    model.addAttribute("page", "fragments/employee-create");
    model.addAttribute("header", "fragments/header");
    return "index";
  }

  @RequestMapping("/insert")
  public String insertEmployee(Model model,
                               @ModelAttribute("employee") EmployeeDTO employee,
                               RedirectAttributes redirectAttributes) {
    return "redirect:/employee";
  }
}
