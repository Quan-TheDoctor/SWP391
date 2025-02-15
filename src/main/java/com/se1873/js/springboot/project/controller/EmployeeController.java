package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.ContractRepository;
import com.se1873.js.springboot.project.service.DepartmentService;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.service.PositionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
  public String employee(Model model,
                         @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                         @AuthenticationPrincipal UserDetails user) {
    log.info("[EMPLOYEE] - employee 323page");

    Pageable pageable = PageRequest.of(page, size, Sort.by("employeeId").ascending());
    Page<EmployeeDTO> employees = employeeService.getAllEmployees(pageable);

    model.addAttribute("direction", direction);
    model.addAttribute("employees", employees);
    model.addAttribute("user",user);
    if (user != null) {
      log.info(user.toString());
    } else {
      log.info("User is null (chưa đăng nhập)");
    }
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("fragments", "fragments/employee");
    return "index";
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId) {
    EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("employee", employeeDTO);
    model.addAttribute("fragments", "fragments/employee-details");
    return "index";
  }

  @RequestMapping("/employee-insert")
  public String viewInsert(Model model) {
    log.info("[EMPLOYEE-INSERT] - employee insert page");
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("employee", new EmployeeDTO());
    model.addAttribute("countContract", contractRepository.count());
    model.addAttribute("fragments", "fragments/employee-insert");
    return "index";
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
                       @RequestParam("value") String value,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
    log.info("[EMPLOYEE] - employee 323page");

    Pageable pageable = PageRequest.of(page, size, Sort.by("employeeId").ascending());
    Integer intValue = "all".equals(value) ? null : Integer.parseInt(value);
    Page<EmployeeDTO> employees = employeeService.filter(field, intValue, pageable);

    model.addAttribute("value", value);
    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("fragments", "fragments/employee");
    return "index";
  }

  @RequestMapping("/employee-export")
  public String export(Model model,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
    log.info("[EXPORT EMPLOYEE] - employee export page");

    Pageable pageable = PageRequest.of(page, size, Sort.by("employeeId").ascending());
    Page<EmployeeDTO> employees = employeeService.getAllEmployees(pageable);

    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("fragments", "fragments/employee");
    return "index";
  }

  @RequestMapping("/sort")
  public String sort(@RequestParam("field") String field,
                     @RequestParam(value = "direction", defaultValue = "asc", required = false) String direction,
                     @RequestParam(value = "page", defaultValue = "0") Integer page,
                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                     Model model) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("employeeId").ascending());
    Page<EmployeeDTO> employees = employeeService.sort(direction, field, pageable);
    log.info(employees.toString());
    model.addAttribute("direction", direction);
    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("fragments", "fragments/employee");
    return "index";
  }
  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("search") String search,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size){
    Pageable pageable = PageRequest.of(page, size, Sort.by("employeeId").ascending());
    Page<EmployeeDTO> employees = employeeService.search(search,pageable);
    model.addAttribute("employees",employees);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("fragments", "fragments/employee");
    return "index";
  }
}
