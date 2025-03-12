package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.service.DepartmentService;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import java.util.Arrays;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/employee")
public class EmployeeController {

  private final EmployeeService employeeService;
  private final EmployeeRepository employeeRepository;
  private final DepartmentService departmentService;
  private final PositionService positionService;
  private List<Department> departments;
  private List<Position> positions;
  private Page<EmployeeDTO> employeeDTOPage;
  private final static String EMPLOYEE = "employee";

  @ModelAttribute
  public void addCommonAttributes(Model model) {
    model.addAttribute("departments", departmentService.getAllDepartments());
    model.addAttribute("positions", positionService.getAllPositions());
  }

  @RequestMapping
  public String employee(
    @PageableDefault(size = 10, sort = "employeeId", direction = Sort.Direction.ASC) Pageable pageable,
    Model model) {

    Page<EmployeeDTO> employees = employeeService.getAll(pageable);
    var totalEmployees = employeeRepository.count();
    var avgSalary = employees.getContent().stream()
      .mapToDouble(EmployeeDTO::getContractBaseSalary)
      .average()
      .orElse(0.0);

    employeeDTOPage = employees;

    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("employees", employees);
    model.addAttribute("currentRequestURI", "/employee");
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    addCommonAttributes(model);
    return "index";
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId) {
    var employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
    model.addAttribute("departments", departments);
    model.addAttribute("employeeDTO", employeeDTO);
    model.addAttribute("contentFragment", "fragments/employee-view-fragments");
    return "index";
  }

  @RequestMapping("/create/form")
  public String createForm(Model model) {

    model.addAttribute("departments", departments);
    model.addAttribute("employeeDTO", new EmployeeDTO());
    model.addAttribute("contentFragment", "fragments/employee-create-fragments");
    return "index";
  }

  @PostMapping("/create/save")
  public String createEmployee(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                               BindingResult bindingResult,
                               Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("departments", departments);
      model.addAttribute("positions", positions);
      model.addAttribute("contentFragment", "fragments/employee-create-fragments");
      return "index";
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
      model.addAttribute("contentFragment", "fragments/employee-view-fragments");
      return "index";
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
    var employees = employeeService.filterByField(field, value, pageable);
    var totalEmployees = employees.getTotalElements();
    var avgSalary = employees.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);

    employeeDTOPage = employees;

    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("employees", employees);
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    return "index";
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

    employeeDTOPage = employees;

    model.addAttribute("employees", employees);
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("departments", departments);
    model.addAttribute("positions", positions);
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    return "index";
  }

  @RequestMapping("/sort")
  public String sort(Model model,
                     @RequestParam("field") String field,
                     @RequestParam("direction") String direction,
                     @RequestParam(value = "page", defaultValue = "0") int page,
                     @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var employees = employeeService.sort(employeeDTOPage, direction, field);

    model.addAttribute("employees", employees);
    model.addAttribute("direction", direction);
    model.addAttribute("currentSortField", field);
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    return "index";
  }

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
    model.addAttribute("employees", employees.getContent());
    model.addAttribute("selectedDepartment", department);
    model.addAttribute("selectedPosition", position);

    addCommonAttributes(model);
    model.addAttribute("contentFragment", "fragments/employee-export-fragments");
    return "index";
  }


  @PostMapping("/export")
  public ResponseEntity<Resource> exportEmployees(
    @RequestParam(value = "selectedEmployees", required = false) String selectedEmployees,
    @RequestParam(value = "department", required = false, defaultValue = "all") String department,
    @RequestParam(value = "position", required = false, defaultValue = "all") String position) {

    log.info("Exporting employee data. Selected IDs: {}, Department: {}, Position: {}", selectedEmployees, department, position);

    List<Integer> employeeIds = (selectedEmployees != null && !selectedEmployees.isEmpty())
      ? Arrays.stream(selectedEmployees.split(",")).map(Integer::parseInt).toList()
      : null;

    Resource file = employeeService.exportToExcel(employeeIds, department, position);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(file);
  }


}
