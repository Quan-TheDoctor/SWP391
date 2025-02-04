package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.dto.EmployeeDTO;
import com.se1873.js.springboot.management.entity.*;
import com.se1873.js.springboot.management.repository.*;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final RoleRepository roleRepository;
  private final SalaryRepository salaryRepository;

  public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository, RoleRepository roleRepository, SalaryRepository salaryRepository) {
    this.employeeRepository = employeeRepository;
    this.departmentRepository = departmentRepository;
    this.positionRepository = positionRepository;
    this.roleRepository = roleRepository;
    this.salaryRepository = salaryRepository;
  }

  public Salary getSalaryByEmployeeId(Integer employeeId) {
    return salaryRepository.findByEmployee_EmployeeIdAndIsPresent(employeeId, true);
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

  public List<EmployeeDTO> filterEmployees(List<EmployeeDTO> employees, String filteredField, Integer value) {
    return employees.stream()
      .filter(employeeDTO -> {
        switch (filteredField.toLowerCase()) {
          case "department":
            return employeeDTO.getDepartmentId() == value;
          case "position":
            return employeeDTO.getPositionId() == value;
          case "role":
            return employeeDTO.getRoleId() == value;
          case "status":
            return employeeDTO.getIsPresent() == (value == 1);
          case "employee":
            return Objects.equals(employeeDTO.getEmployeeId(), value);
          default:
            return true;
        }
      })
      .toList();
  }

  public void updateEmployee(EmployeeDTO employeeDTO) {
    Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeDTO.getEmployeeId());
    employee.setEmployeeCode(employeeDTO.getEmployeeCode());
    employee.setEmployeeName(employeeDTO.getEmployeeName());
    employee.setEmployeeAddress(employeeDTO.getEmployeeAddress());
    employee.setEmployeeEmail(employeeDTO.getEmployeeEmail());
    employee.setEmployeePhone(employeeDTO.getEmployeePhone());
    employee.setPresent(employeeDTO.getIsPresent());
    employeeRepository.save(employee);
  }

  private EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
    Department department = getDepartmentByEmployeeId(employee.getEmployeeId());
    Position position = getPositionByEmployeeId(employee.getEmployeeId());
    Role role = getRoleByEmployeeId(employee.getEmployeeId());
    Salary salary = getSalaryByEmployeeId(employee.getEmployeeId());

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
      .salaryId(salary.getSalaryId())
      .salaryAmount(formatSalaryAmount(salary.getSalaryAmount()))
      .salaryStartDate(salary.getStartDate())
      .salaryEndDate(salary.getEndDate())
      .isPresent(employee.getIsPresent())
      .build();
  }
  public String formatSalaryAmount(Double salaryAmount) {
    DecimalFormat formatter = new DecimalFormat("#,###");
    return formatter.format(salaryAmount);
  }
  private Function<EmployeeDTO, Comparable> fieldsMap(String field) {
    Map<String, Function<EmployeeDTO, Comparable>> fieldsMap = new HashMap<>();
    fieldsMap.put("employeeCode", EmployeeDTO::getEmployeeCode);
    fieldsMap.put("employeeName", EmployeeDTO::getEmployeeName);
    fieldsMap.put("departmentName", EmployeeDTO::getDepartmentName);
    fieldsMap.put("positionName", EmployeeDTO::getPositionName);
    fieldsMap.put("roleName", EmployeeDTO::getRoleName);
    fieldsMap.put("salaryAmount", EmployeeDTO::getSalaryAmount);
    fieldsMap.put("isPresent", EmployeeDTO::getIsPresent);
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
