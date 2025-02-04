package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.dto.EmployeeDTO;
import com.se1873.js.springboot.management.entity.Department;
import com.se1873.js.springboot.management.entity.Position;
import com.se1873.js.springboot.management.entity.Role;
import com.se1873.js.springboot.management.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
  private final SalaryService salaryService;

  public EmployeeController(EmployeeService employeeService, DepartmentService departmentService, PositionService positionService, DepartmentEmployeeService departmentEmployeeService, RoleService roleService, SalaryService salaryService) {
    this.employeeService = employeeService;
    this.departmentService = departmentService;
    this.positionService = positionService;
    this.departmentEmployeeService = departmentEmployeeService;
    this.roleService = roleService;
    this.salaryService = salaryService;
  }

  List<EmployeeDTO> employees = new ArrayList<>();
  List<Department> departments = new ArrayList<>();
  List<Position> positions = new ArrayList<>();
  List<Role> roles = new ArrayList<>();

  private static final String bodyContent = "bodyContent";
  private static final String index = "index";

  @PostConstruct
  public void init() {
    employees = employeeService.getAllEmployees();
    departments = departmentService.findAll();
    positions = positionService.findAll();
    roles = roleService.findAll();
  }

  public String getService(String service) {
    switch (service) {
      case "sort":
        return service;
    }
    return null;
  }

  @RequestMapping()
  public String index(Model model,
                      @RequestParam(value = "service", required = false, defaultValue = "null") String service,
                      @RequestParam(value = "sortedField", required = false, defaultValue = "null") String sortedField,
                      @RequestParam(value = "direction", required = false, defaultValue = "null") String direction,
                      @ModelAttribute(value = "modifiedEmployees") List<EmployeeDTO> modifiedEmployees) {
    List<EmployeeDTO> tempList = new ArrayList<>();
    if("sort".equals(service)) {
      tempList = modifiedEmployees;
      model.addAttribute("sortedField", sortedField);
      model.addAttribute("direction", direction);
    }

    if(modifiedEmployees == null) {
      tempList = employees;
    }


    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("employees", tempList);
    model.addAttribute("allEmployees", employees);
    model.addAttribute("roles", roles);
    model.addAttribute("page", "employee");
    model.addAttribute(bodyContent, "fragments/employee");
    return index;
  }

  @GetMapping(value = "/sort")
  public String sort(Model model,
                     @RequestParam("sortedField") String sortedField,
                     @RequestParam("direction") String direction,
                     RedirectAttributes redirectAttributes) {
    var tempList = employeeService.sortEmployees(employees, sortedField, direction);

    redirectAttributes.addFlashAttribute("modifiedEmployees", tempList);
    return "redirect:/employee?service=sort" + "&sortedField=" + sortedField + "&direction=" + direction;
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

  @GetMapping(value = "/filter")
  public String filter(Model model,
                       @RequestParam("filteredField") String filteredField,
                       @RequestParam("value") Integer value) {
    model.addAttribute("allEmployees", employees);
    model.addAttribute("employees", employeeService.filterEmployees(employees, filteredField, value));
    model.addAttribute("filteredField", filteredField);
    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("roles", roles);
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
    } else if("salary-information".equalsIgnoreCase(editSection)) {
      salaryService.updateSalary(employee.getEmployeeId());
      salaryService.save(employee);
    }
    init();
    return "redirect:/employee";
  }

  @GetMapping("/search")
  public String search(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("id") Integer id) {


    model.addAttribute("employees", employeeService.filterEmployees(employees, field, id));
    model.addAttribute("allEmployees", employees);
    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    model.addAttribute("roles", roles);
    model.addAttribute("page", "employee");
    model.addAttribute(bodyContent, "fragments/employee");
    return index;
  }
}
