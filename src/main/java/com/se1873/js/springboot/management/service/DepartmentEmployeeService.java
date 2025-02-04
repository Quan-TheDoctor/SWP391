package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.entity.*;
import com.se1873.js.springboot.management.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;

@Service
public class DepartmentEmployeeService {
  private final DepartmentEmployeeRepository departmentEmployeeRepository;
  private final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;
  private final PositionRepository positionRepository;
  private final RoleRepository roleRepository;

  public DepartmentEmployeeService(DepartmentEmployeeRepository departmentEmployeeRepository, DepartmentRepository departmentRepository, EmployeeRepository employeeRepository, PositionRepository positionRepository, RoleRepository roleRepository) {
    this.departmentEmployeeRepository = departmentEmployeeRepository;
    this.departmentRepository = departmentRepository;
    this.employeeRepository = employeeRepository;
    this.positionRepository = positionRepository;
    this.roleRepository = roleRepository;
  }

  public void updateDepartmentEmployee(Integer employeeId) {
    DepartmentEmployee de = departmentEmployeeRepository.findByEmployeeId(employeeId);
    if (de == null) {
      throw new RuntimeException("DepartmentEmployee with employeeId " + employeeId + " not found.");
    }
    de.setEndDate(LocalDate.now());
    de.setIsPresent(false);

    departmentEmployeeRepository.save(de);
  }

  public void save(Integer employeeId, Integer departmentId, Integer positionId, Integer roleId) {
    Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeId);
    Department department = departmentRepository.findDepartmentByDepartmentId(departmentId);
    Position position = positionRepository.findPositionByPositionId(positionId);
    Role role = roleRepository.findRoleByRoleId(roleId);

    DepartmentEmployee departmentEmployee = DepartmentEmployee
      .builder()
      .employee(employee)
      .department(department)
      .position(position)
      .role(role)
      .startDate(LocalDate.now())
      .endDate(null)
      .isPresent(true)
      .build();

    departmentEmployeeRepository.save(departmentEmployee);
  }
}
