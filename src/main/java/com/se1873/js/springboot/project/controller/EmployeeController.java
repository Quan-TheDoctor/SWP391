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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    model.addAttribute("positions",positions);
    model.addAttribute("employees", employees);
    return "employee";
  }

  @RequestMapping("/view")
  public String view(Model model,
                     @RequestParam("employeeId") Integer employeeId) {

    var employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
    log.error(employeeDTO.toString());
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

  @RequestMapping("/create/save")
  public String createEmployee(Model model,
                               @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                               BindingResult bindingResult) {
    log.info(employeeDTO.toString());
    if(bindingResult.hasErrors()) {
      return "redirect:/employee";
    }

    employeeService.saveEmployee(employeeDTO);

    return "redirect:/employee";
  }
  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") String value,
                       @RequestParam(value = "page",defaultValue = "0")  int page,
                       @RequestParam(value = "size", defaultValue = "10") int size){
    Pageable pageable = PageRequest.of(page,size);
  var employeeDTOS = employeeService.filterByField(field,value,pageable);
  var totalEmployees = employeeDTOS.getTotalElements();
  var avgSalary = employeeDTOS.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);
  model.addAttribute("totalEmployees", totalEmployees);
  model.addAttribute("avgSalary", avgSalary);
  model.addAttribute("departments", departments);
  model.addAttribute("positions",positions);
  model.addAttribute("employees",employeeDTOS);

    return "employee";
  }
  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("query") String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size){
    Pageable pageable = PageRequest.of(page,size);
    var employees = employeeService.search(pageable,query);
    var totalEmployees = employees.getTotalElements();
    var avgSalary = employees.getContent().stream().mapToDouble(EmployeeDTO::getContractBaseSalary).average().orElse(0.0);
    model.addAttribute("employees",employees);
    model.addAttribute("totalEmployees",totalEmployees);
    model.addAttribute("avgSalary",avgSalary);
    model.addAttribute("departments",departments);
    model.addAttribute("positions",positions);
    return "employee";
  }
}
