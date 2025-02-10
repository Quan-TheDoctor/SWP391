package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.ContractRepository;
import com.se1873.js.springboot.project.service.DepartmentService;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.service.PositionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/employee")
public class EmployeeController {
  private final DepartmentService departmentService;
  private final PositionService positionService;
  private final ContractRepository contractRepository;
  private final EmployeeService employeeService;
  private List<Department> departments;
  private List<Position> positions;

  @PostConstruct
  public void init() {
    departments = departmentService.getAllDepartments();
    positions = positionService.getAllPositions();
  }

  @RequestMapping
  public String employee(Model model) {
    log.info("[EMPLOYEE] - employee 323page");
    List<EmployeeDTO> employees = employeeService.getAllEmployees();

    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    return "employee";
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId) {
    EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
    model.addAttribute("employee", employeeDTO);
    return "employee-details";
  }

  @RequestMapping("/employee-insert")
  public String viewInsert(Model model) {
    log.info("[EMPLOYEE-INSERT] - employee insert page");
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("employee", new EmployeeDTO());
    model.addAttribute("countContract", contractRepository.count());
    return "employee-insert";
  }

  @RequestMapping("/insert")
  public String insert(Model model,
                       @ModelAttribute("employee") EmployeeDTO employeeDTO) {
    log.info("[INSERT] - employee add");
    employeeService.insertEmployee(employeeDTO);
    return "redirect:/employee";
  }
  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") Integer value){
    List<EmployeeDTO> employees =  employeeService.filter(field,value);

    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    return "employee";
  }
}
