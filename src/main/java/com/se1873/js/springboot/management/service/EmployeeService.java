package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.dto.EmployeeDTO;
import com.se1873.js.springboot.management.entity.Department;
import com.se1873.js.springboot.management.entity.Employee;
import com.se1873.js.springboot.management.entity.Position;
import com.se1873.js.springboot.management.entity.Role;
import com.se1873.js.springboot.management.repository.DepartmentRepository;
import com.se1873.js.springboot.management.repository.EmployeeRepository;
import com.se1873.js.springboot.management.repository.PositionRepository;
import com.se1873.js.springboot.management.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class EmployeeService {
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final RoleRepository roleRepository;

  public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository, RoleRepository roleRepository) {
    this.employeeRepository = employeeRepository;
    this.departmentRepository = departmentRepository;
    this.positionRepository = positionRepository;
    this.roleRepository = roleRepository;
  }

  public Role getRoleByEmployeeId(Integer employeeId) {
    return roleRepository.findRoleByEmployeeId(employeeId);
  }

  public Department getDepartmentByEmployeeId(Integer employeeId) {
    return departmentRepository.findDepartmentByEmployeeId(employeeId);
  }

  public Position getPositionByEmployeeId(Integer employeeId) {
    return positionRepository.findPositionByEmployeeId(employeeId);
  }

  public EmployeeDTO getEmployeeById(int id) {
    return convertEmployeeToEmployeeDTO(employeeRepository.findEmployeeByEmployeeId(id));
  }

  public List<EmployeeDTO> getAllEmployees() {
    return employeeRepository
      .findAll()
      .stream()
      .map(this::convertEmployeeToEmployeeDTO)
      .sorted(comparator("employeeCode", "asc"))
      .toList();
  }

  public List<EmployeeDTO> sortEmployees(List<EmployeeDTO> employees, String field, String direction) {
    return employees
      .stream()
      .sorted(comparator(field, direction))
      .toList();
  }

  public void updateEmployee(EmployeeDTO employeeDTO) {
    Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeDTO.getEmployeeId());
    employee.setEmployeeCode(employeeDTO.getEmployeeCode());
    employee.setEmployeeName(employeeDTO.getEmployeeName());
    employee.setEmployeeAddress(employeeDTO.getEmployeeAddress());
    employee.setEmployeeEmail(employeeDTO.getEmployeeEmail());
    employee.setEmployeePhone(employeeDTO.getEmployeePhone());

    employeeRepository.save(employee);
  }

  private EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
    Department department = getDepartmentByEmployeeId(employee.getEmployeeId());
    Position position = getPositionByEmployeeId(employee.getEmployeeId());
    Role role = getRoleByEmployeeId(employee.getEmployeeId());
    return EmployeeDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeCode(employee.getEmployeeCode())
      .employeeName(employee.getEmployeeName())
      .employeePhone(employee.getEmployeePhone())
      .employeeEmail(employee.getEmployeeEmail())
      .employeeAddress(employee.getEmployeeAddress())
      .departmentId(department.getDepartmentId())
      .departmentName(department.getDepartmentName())
      .positionId(position.getPositionId())
      .positionName(position.getPositionName())
      .roleId(role.getRoleId())
      .roleName(role.getRoleName())
      .build();
  }

  private Function<EmployeeDTO, Comparable> fieldsMap(String field) {
    Map<String, Function<EmployeeDTO, Comparable>> fieldsMap = new HashMap<>();
    fieldsMap.put("employeeCode", EmployeeDTO::getEmployeeCode);
    fieldsMap.put("employeeName", EmployeeDTO::getEmployeeName);
    fieldsMap.put("departmentName", EmployeeDTO::getDepartmentName);
    fieldsMap.put("positionName", EmployeeDTO::getPositionName);
    fieldsMap.put("roleName", EmployeeDTO::getRoleName);
    return fieldsMap.get(field);
  }

  private Comparator<EmployeeDTO> comparator(String field, String direction) {
    var flag = "asc".equalsIgnoreCase(direction);

    Function<EmployeeDTO, Comparable> fieldsMap = fieldsMap(field);
    if(fieldsMap == null) throw new IllegalArgumentException("Unknown field: " + field);
    Comparator<EmployeeDTO> comparator = Comparator.comparing(
      fieldsMap,
      (val1, val2) -> {
        if (val1 == null && val2 == null) return 0;
        if (val1 == null) return 1;
        if (val2 == null) return -1;
        return (val1).compareTo(val2);
      }
//      flag ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
    );
    comparator = flag ? comparator : comparator.reversed();
    return comparator;
  }
}
