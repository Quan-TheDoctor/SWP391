package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.DepartmentDTO;
import com.se1873.js.springboot.project.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
  Department findDepartmentByDepartmentId(Integer departmentId);
}
