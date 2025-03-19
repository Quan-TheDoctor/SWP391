package com.se1873.js.springboot.project.service.department.query;

import com.se1873.js.springboot.project.entity.Department;

import java.util.List;

public interface DepartmentQueryService {
  List<Department> getAllDepartments();
  Department findDepartmentByDepartmentId(Integer departmentId);
}
