package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.dto.EmployeeDTO;
import com.se1873.js.springboot.management.entity.Department;
import com.se1873.js.springboot.management.entity.Employee;
import com.se1873.js.springboot.management.entity.Position;
import com.se1873.js.springboot.management.service.DepartmentService;
import com.se1873.js.springboot.management.service.EmployeeService;
import com.se1873.js.springboot.management.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {
  @Autowired
  private EmployeeService employeeService;
  @Autowired
  private DepartmentService departmentService;
  @Autowired
  private PositionService positionService;

  List<EmployeeDTO> employees = new ArrayList<>();
  List<Department> departments = new ArrayList<>();
  List<Position> positions = new ArrayList<>();

  @RequestMapping()
  public String index(Model model) {
    employees = employeeService.getAllEmployees();
    departments = departmentService.findAll();
    positions = positionService.findAll();

    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("employees", employees);
    model.addAttribute("field", "");
    model.addAttribute("sorted", false);
    model.addAttribute("page", "employee");
    model.addAttribute("bodyContent", "fragments/employee");
    return "index";
  }

  @GetMapping(value = "/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId,
                     @RequestParam("edit") Boolean edit,
                     @RequestParam("editSection") String editSection) {
    EmployeeDTO employee = employeeService.getEmployeeById(employeeId);

    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("employee", employee);
    model.addAttribute("edit", edit);
    model.addAttribute("editSection", editSection);
    model.addAttribute("page", "employee");
    model.addAttribute("bodyContent", "fragments/employee-detail");
    return "index";
  }

  @GetMapping(value = "/sort")
  public String sort(Model model,
                     @RequestParam("sortedField") String sortedField,
                     @RequestParam("direction") String direction) {

    model.addAttribute("sortedField", sortedField);
    model.addAttribute("direction", direction);
    model.addAttribute("employees", employeeService.sortEmployees(employees, sortedField, direction));
    model.addAttribute("page", "employee");
    model.addAttribute("bodyContent", "fragments/employee");
    return "index";
  }
}
