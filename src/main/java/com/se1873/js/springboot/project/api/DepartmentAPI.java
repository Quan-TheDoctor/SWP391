package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.DepartmentDTO;
import com.se1873.js.springboot.project.mapper.DepartmentDTOMapper;
import com.se1873.js.springboot.project.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/department")
public class DepartmentAPI {
  private final DepartmentService departmentService;
  private final DepartmentDTOMapper departmentDTOMapper;

  @GetMapping("/getAll")
  public ResponseEntity<List<DepartmentDTO>> getAll() {
    var departments = departmentService.getAllDepartments().stream().map(departmentDTOMapper::toDTO).toList();

    return ResponseEntity.ok(departments);
  }
}
