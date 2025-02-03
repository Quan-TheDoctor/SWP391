package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.dto.EmployeeDTO;
import com.se1873.js.springboot.management.entity.Department;
import com.se1873.js.springboot.management.entity.Position;
import com.se1873.js.springboot.management.entity.Role;
import com.se1873.js.springboot.management.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final PositionService positionService;
  private final DepartmentEmployeeService departmentEmployeeService;
  private final RoleService roleService;

  public EmployeeController(EmployeeService employeeService, DepartmentService departmentService, PositionService positionService, DepartmentEmployeeService departmentEmployeeService, RoleService roleService) {
    this.employeeService = employeeService;
    this.departmentService = departmentService;
    this.positionService = positionService;
    this.departmentEmployeeService = departmentEmployeeService;
    this.roleService = roleService;
  }

  List<EmployeeDTO> employees = new ArrayList<>();
  List<Department> departments = new ArrayList<>();
  List<Position> positions = new ArrayList<>();
  List<Role> roles = new ArrayList<>();

  private static final String bodyContent = "bodyContent";
  private static final String index = "index";
  @RequestMapping()
  public String index(Model model) {
    employees = employeeService.getAllEmployees();
    departments = departmentService.findAll();
    positions = positionService.findAll();
    roles = roleService.findAll();

    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("employees", employees);
    model.addAttribute("roles", roles);
    model.addAttribute("field", "");
    model.addAttribute("sorted", false);
    model.addAttribute("page", "employee");
    model.addAttribute(bodyContent, "fragments/employee");
    return index;
  }

  @GetMapping(value = "/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId,
                     @RequestParam("edit") Boolean edit,
                     @RequestParam("editSection") String editSection) {
    EmployeeDTO employee = employeeService.getEmployeeById(employeeId);

    model.addAttribute("roles", roles);
    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("employee", employee);
    model.addAttribute("edit", edit);
    model.addAttribute("editSection", editSection);
    model.addAttribute("page", "employee");
    model.addAttribute(bodyContent, "fragments/employee-detail");
    return index;
  }

  @GetMapping(value = "/sort")
  public String sort(Model model,
                     @RequestParam("sortedField") String sortedField,
                     @RequestParam("direction") String direction) {

    model.addAttribute("sortedField", sortedField);
    model.addAttribute("direction", direction);
    model.addAttribute("employees", employeeService.sortEmployees(employees, sortedField, direction));
    model.addAttribute("page", "employee");
    model.addAttribute(bodyContent, "fragments/employee");
    return index;
  }

  @GetMapping(value = "/update")
  public String update(@ModelAttribute("employee") EmployeeDTO employee,
                       @RequestParam("editSection") String editSection) {
    if("basic-information".equalsIgnoreCase(editSection)) {
      employeeService.updateEmployee(employee);
    } else if("job-information".equalsIgnoreCase(editSection)) {
      departmentEmployeeService.updateDepartmentEmployee(employee.getEmployeeId());
      departmentEmployeeService.save(employee.getEmployeeId(), employee.getDepartmentId(), employee.getPositionId(), employee.getRoleId());
    }
    return "redirect:/employee";
  }
}
