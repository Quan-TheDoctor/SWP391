package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/employee")
public class EmployeeAPI {
    private final EmployeeService employeeService;

    @RequestMapping("/employeeAvailable")
    public ResponseEntity<EmployeeCountDTO> employeeAvailable() {
        EmployeeCountDTO employeeDTO = employeeService.getEmployeeDTOIsCurrent();
        return ResponseEntity.ok(employeeDTO);
    }
}
