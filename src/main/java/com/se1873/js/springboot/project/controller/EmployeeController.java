package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.User;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
  private final GlobalController globalController;
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
    Model model,
    @ModelAttribute("loggedInUser") User loggedInUser) {
    Page<EmployeeDTO> employees = employeeService.getAll(pageable);
    var totalEmployees = employeeRepository.count();
    var avgSalary = employees.getContent().stream()
      .mapToDouble(EmployeeDTO::getContractBaseSalary)
      .average()
      .orElse(0.0);

    employeeDTOPage = employees;

    addCommonAttributes(model);
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("employees", employees);
    model.addAttribute("currentRequestURI", "/employee");
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    return "index";
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId,
                     @ModelAttribute("loggedInUser") User loggedInUser) {
    var employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);

    log.info(employeeDTO.toString());
    addCommonAttributes(model);
    globalController.createAuditLog(loggedInUser, "View details of Employee #" + employeeDTO.getEmployeeId() , "View", "Normal");
    model.addAttribute("employeeDTO", employeeDTO);
    model.addAttribute("currentRequestURI", "/employee");
    model.addAttribute("contentFragment", "fragments/employee-view-fragments");
    return "index";
  }

  @RequestMapping("/create/form")
  public String createForm(Model model) {

    addCommonAttributes(model);
    model.addAttribute("employeeDTO", new EmployeeDTO());
    model.addAttribute("contentFragment", "fragments/employee-create-fragments");
    return "index";
  }
  @PostMapping("/create/save")
  public String saveEmployee(@ModelAttribute EmployeeDTO employeeDTO,
                             @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                             RedirectAttributes redirectAttributes) {
    try {
      if (avatarFile != null && !avatarFile.isEmpty()) {
        employeeDTO.setPicture(avatarFile.getBytes());
      }

      employeeService.saveEmployee(employeeDTO);
      redirectAttributes.addFlashAttribute("message", "Employee created successfully");
      return "redirect:/employee";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", "Error creating employee: " + e.getMessage());
      return "redirect:/employee/create/form";
    }
  }

  @GetMapping("/avatar/{id}")
  public ResponseEntity<byte[]> getAvatar(@PathVariable Integer id) {
    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(id);
    if (employee != null && employee.getPicture() != null) {
      return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .body(employee.getPicture());
    }
    return ResponseEntity.notFound().build();
  }


  @PostMapping("/create/update")
  public String updateEmployee(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                               BindingResult bindingResult,
                               Model model,
                               @ModelAttribute("loggedInUser") User loggedInUser, RedirectAttributes redirectAttributes) {

    if (bindingResult.hasErrors()) {
      addCommonAttributes(model);
      globalController.createAuditLog(loggedInUser, "Update Employee ID #" + employeeDTO.getEmployeeId() , "Error", "Normal");
      globalController.sendMessage(redirectAttributes, bindingResult.getFieldError().getDefaultMessage(), "Error");
      return "redirect:/employee";
    }

    globalController.createAuditLog(loggedInUser, "Update Employee ID #" + employeeDTO.getEmployeeId() , "Update", "Normal");
    employeeService.saveEmployee(employeeDTO);
    globalController.sendMessage(redirectAttributes, "Update employee ID #" + employeeDTO.getEmployeeId() + " successfully", "success");
    return "redirect:/employee";
  }

  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") String value,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @ModelAttribute("loggedInUser") User loggedInUser) {
    Pageable pageable = PageRequest.of(page, size);
    var employees = employeeService.filterByField(field, value, pageable);
    var totalEmployees = employees.getTotalElements();
    var avgSalary = employees.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);

    employeeDTOPage = employees;

    globalController.createAuditLog(loggedInUser, "Filter list Employee by " + field , "View", "Normal");
    addCommonAttributes(model);
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("employees", employees);
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    return "index";
  }

  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("query") String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @ModelAttribute("loggedInUser") User loggedInUser) {
    Pageable pageable = PageRequest.of(page, size);
    var employees = employeeService.search(pageable, query);
    var totalEmployees = employees.getTotalElements();
    var avgSalary = employees.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);

    employeeDTOPage = employees;

    globalController.createAuditLog(loggedInUser, "Search Employee by " + query , "View", "Normal");
    addCommonAttributes(model);
    model.addAttribute("employees", employees);
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("avgSalary", avgSalary);
    model.addAttribute("contentFragment", "fragments/employee-fragments");
    return "index";
  }

  @RequestMapping("/sort")
  public String sort(Model model,
                     @RequestParam("field") String field,
                     @RequestParam("direction") String direction,
                     @RequestParam(value = "page", defaultValue = "0") int page,
                     @RequestParam(value = "size", defaultValue = "10") int size,
                     @ModelAttribute("loggedInUser") User loggedInUser) {
    Pageable pageable = PageRequest.of(page, size);
    var employees = employeeService.sort(employeeDTOPage, direction, field);

    globalController.createAuditLog(loggedInUser, "Sort Employee by " + field + " in " + direction + " order", "View", "Normal");
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
    @RequestParam(value = "position", required = false, defaultValue = "all") String position,
    @ModelAttribute("loggedInUser") User loggedInUser) {

    List<Integer> employeeIds = (selectedEmployees != null && !selectedEmployees.isEmpty())
      ? Arrays.stream(selectedEmployees.split(",")).map(Integer::parseInt).toList()
      : null;

    Resource file = employeeService.exportToExcel(employeeIds, department, position);

    globalController.createAuditLog(loggedInUser, "Exports list Employees" , "Export", "Normal");
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(file);
  }


}
