package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.PositionRepository;
import com.se1873.js.springboot.project.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/employee")
public class EmployeeController {

  private final EmployeeService employeeService;
  private final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;
  private final PositionRepository positionRepository;
  private List<Department> departments;
  private List<Position> positions;

  private  final static  String EMPLOYEE = "employee";

  @PostConstruct
  public void init() {
    positions = positionRepository.findAll();
    departments = departmentRepository.findAll();
  }

  @RequestMapping
  public String employee(Model model) {

    var employees = employeeService.getAll(PageRequest.of(0, 10));
    var totalEmployees = employeeRepository.count();

    var avgSalary = employees.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().getAsDouble();
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("employees", employees);
    return EMPLOYEE;
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId) {
    var employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
    model.addAttribute("departments", departments);
    model.addAttribute("employeeDTO", employeeDTO);
    return "employee-view";
  }

  @RequestMapping("/create/form")
  public String createForm(Model model) {

    model.addAttribute("departments", departments);
    model.addAttribute("employeeDTO", new EmployeeDTO());
    return "employee-create";
  }

  @PostMapping("/create/save")
  public String createEmployee(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                               BindingResult bindingResult,
                               Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("departments", departments);
      model.addAttribute("positions", positions);
      return "employee-create";
    }

    employeeService.saveEmployee(employeeDTO);
    return "redirect:/employee";
  }

  @PostMapping("/create/update")
  public String updateEmployee(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                               BindingResult bindingResult,
                               Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("departments", departments);
      model.addAttribute("positions", positions);
      return "employee-view";
    }

    employeeService.saveEmployee(employeeDTO);
    return "redirect:/employee";
  }

  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") String value,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var employeeDTOS = employeeService.filterByField(field, value, pageable);
    var totalEmployees = employeeDTOS.getTotalElements();
    var avgSalary = employeeDTOS.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("employees", employeeDTOS);
    return "employee";
  }

  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("query") String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var employees = employeeService.search(pageable, query);
    var totalEmployees = employees.getTotalElements();
    var avgSalary = employees.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);
    model.addAttribute("employees", employees);
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    return "employee";
  }
//  @RequestMapping("/sort")
//  public String sort(Model model,
//                     @RequestParam("field") String field,
//                     @RequestParam("direction") String direction,
//                     @RequestParam(value = "page",defaultValue = "0") int page,
//                     @RequestParam(value = "size",defaultValue = "10") int size){
//    Pageable pageable = PageRequest.of(page,size);
//    var employees = employeeService.sort(pageable,direction);
//
//    model.addAttribute("employees",employees);
//    model.addAttribute("direction",direction);
//    model.addAttribute("currentSortField", field);
//    return "employee";
//  }

  @RequestMapping("/export/view")
  public String exportView(Model model,
                           @RequestParam(value = "department", required = false, defaultValue = "all") String department,
                           @RequestParam(value = "position", required = false, defaultValue = "all") String position,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var employees = employeeService.exportFilteredEmployees(department, position, pageable);
    var totalEmployees = employees.getTotalElements();

    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("currentPage", page + 1);
    model.addAttribute("totalPages", employees.getTotalPages());
    model.addAttribute("departments", departmentRepository.findAll());
    model.addAttribute("positions", positionRepository.findAll());
    model.addAttribute("employees", employees.getContent());

    model.addAttribute("selectedDepartment", department);
    model.addAttribute("selectedPosition", position);

    return "employee-export";
  }


  @RequestMapping("/export")
  public ResponseEntity<Resource> exportEmployees(
    @RequestParam(value = "department", required = false, defaultValue = "all") String department,
    @RequestParam(value = "position", required = false, defaultValue = "all") String position) {

    log.info("Exporting employee data to Excel. Department: {}, Position: {}", department, position);
    Resource file = employeeService.exportToExcel(department, position);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(file);
  }

}
