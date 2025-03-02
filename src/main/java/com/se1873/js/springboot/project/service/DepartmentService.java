package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.DepartmentDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;

  public List<Department> getAllDepartments() {
    return departmentRepository.findAll();
  }

  private DepartmentDTO convertDepartmentToDepartmentDTO(Department department) {
    return DepartmentDTO
      .builder()
      .departmentId(department.getDepartmentId())
      .departmentName(department.getDepartmentName())
      .build();
  }
}
