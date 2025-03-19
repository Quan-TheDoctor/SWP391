package com.se1873.js.springboot.project.service.department.query;

import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentQueryServiceImpl implements DepartmentQueryService {
  private final DepartmentRepository departmentRepository;

  public DepartmentQueryServiceImpl(DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
  }

  @Override
  public List<Department> getAllDepartments() {
    return departmentRepository.findAll();
  }

  @Override
  public Department findDepartmentByDepartmentId(Integer departmentId) {
    return departmentRepository.findDepartmentByDepartmentId(departmentId);
  }
}
