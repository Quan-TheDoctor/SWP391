package com.se1873.js.springboot.project.service.department;

import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.mapper.DepartmentDTOMapper;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.service.department.query.DepartmentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DepartmentService {
  private final DepartmentDTOMapper departmentDTOMapper;

  private final DepartmentRepository departmentRepository;
  private final DepartmentQueryService departmentQueryService;

  @Cacheable(value = "departments", key = "'allDepartments'")
  public List<Department> getAllDepartments() {
    log.info("Loading departments from DB...");
    return departmentQueryService.getAllDepartments();
  }

  @CacheEvict(value = "departments", allEntries = true)
  public void refreshDepartmentsCache() {
    log.info("Clearing departments cache...");
  }

  public Department findDepartmentByDepartmentId(Integer departmentId) {
    return departmentQueryService.findDepartmentByDepartmentId(departmentId);
  }


}
