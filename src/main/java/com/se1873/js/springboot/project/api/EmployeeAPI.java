package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/employee")
public class EmployeeAPI {

  private final EmployeeService employeeService;

  @RequestMapping("/employeeAvailable")
    public ResponseEntity<EmployeeCountDTO> employeeAvailable() {
        EmployeeCountDTO employeeDTO = employeeService.getAvailableAndUnavailableEmployeeCount();
        return ResponseEntity.ok(employeeDTO);
    }
  @GetMapping("/getAllByDepartmentId")
  public ResponseEntity<List<EmployeeDTO>> getAllByDepartmentId(@RequestParam("departmentId") int departmentId) {
    Page<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(departmentId, PageRequest.of(0, 10));
    return ResponseEntity.ok(employees.getContent());
  }
  @GetMapping("/getAll")
  public ResponseEntity<List<EmployeeDTO>> getAll() {
    List<EmployeeDTO> employees = employeeService.getAllEmployees();
    return ResponseEntity.ok(employees);
  }
}
