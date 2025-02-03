package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.entity.Department;
import com.se1873.js.springboot.management.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  DepartmentService(DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
  }

  public List<Department> findAll() {
    return departmentRepository.findAll();
  }
}
