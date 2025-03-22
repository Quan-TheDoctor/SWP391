package com.se1873.js.springboot.project.service.employee.query;

import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.mapper.EmployeeDTOMapper;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeQueryServiceImpl implements EmployeeQueryService {
  private final EmployeeRepository employeeRepository;
  private final EmployeeDTOMapper employeeDTOMapper;

  public EmployeeQueryServiceImpl(EmployeeRepository employeeRepository,
                                  EmployeeDTOMapper employeeDTOMapper) {
    this.employeeRepository = employeeRepository;
    this.employeeDTOMapper = employeeDTOMapper;
  }

  @Override
  public List<EmployeeDTO> getAll() {
     return employeeRepository.findAll()
        .stream()
        .filter(e -> !e.getIsDeleted())
        .map(employeeDTOMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public Double getAverageSalary(List<EmployeeDTO> employees) {
    return employees
      .stream()
      .mapToDouble(EmployeeDTO::getContractBaseSalary)
      .average()
      .orElse(0.0);
  }

  @Override
  public Page<Employee> search(String query, Pageable pageable) {
    return employeeRepository.searchEmployee(query, pageable);
  }

  @Override
  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return employeeDTOMapper.toDTO(employeeRepository.getEmployeeByEmployeeIdAndIsDeleted(employeeId, false));
  }

  @Override
  public List<Employee> getEmployeesByDepartmentId(Integer departmentId) {
    return employeeRepository.findEmployeesByIsDeleted(false);
  }

  @Override
  public Page<EmployeeDTO> getEmployeesByDepartmentId(Integer departmentId, Pageable pageable) {
    List<EmployeeDTO> employees = employeeRepository.findAll(pageable)
      .stream()
      .map(employeeDTOMapper::toDTO)
      .filter(e -> !e.getIsDeleted() && e.getDepartmentId().equals(departmentId))
      .collect(Collectors.toList());

    return new PageImpl<>(employees, pageable, employees.size());
  }

  @Override
  public Page<EmployeeDTO> getAll(Pageable pageable) {
    List<EmployeeDTO> employeePage = employeeRepository.findAll(pageable)
      .stream()
      .filter(e -> !e.getIsDeleted())
      .map(employeeDTOMapper::toDTO)
      .collect(Collectors.toList());

    return new PageImpl<>(employeePage, pageable, employeePage.size());
  }

  @Override
  public Page<EmployeeDTO> filter(String field, String value, Pageable pageable) {
    return switch (field.toLowerCase()) {
      case "department" -> employeeRepository.findEmployeesByDepartmentName(value, pageable).map(employeeDTOMapper::toDTO);
      case "position" -> employeeRepository.findEmployeesByPositionName(value, pageable).map(employeeDTOMapper::toDTO);
      case "all" -> employeeRepository.findAll(pageable).map(employeeDTOMapper::toDTO);
      default -> throw new IllegalArgumentException("Invalid field: " + field);
    };
  }

  @Override
  public List<EmployeeDTO> sort(Page<EmployeeDTO> source, String direction, String field) {
    List<EmployeeDTO> sorted = source.getContent().stream()
      .sorted(getComparator(field, direction))
      .collect(Collectors.toList());
    return sorted;
  }

  @Override
  public EmployeeCountDTO getAvailableAndUnavailableEmployeeCount() {
    Integer availableCount = 0;
    Integer unavailableCount = 0;
    List<EmployeeDTO> employeeDTO = employeeRepository.findAll()
      .stream()
      .map(employeeDTOMapper::toDTO)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    for (EmployeeDTO emp : employeeDTO) {
      if (Boolean.TRUE.equals(emp.getContractIsPresent()) &&
        Boolean.TRUE.equals(emp.getEmploymentHistoryIsCurrent())) {
        availableCount++;
      } else {
        unavailableCount++;
      }
    }
    return EmployeeCountDTO.builder()
      .totalAvailableEmployees(availableCount)
      .totalUnavailableEmployees(unavailableCount)
      .build();
  }

  @Override
  public Integer getEmployeeCount() {
    return employeeRepository.getEmployeeCount();
  }

  private java.util.Comparator<EmployeeDTO> getComparator(String field, String direction) {
    return switch (field) {
      case "firstName" ->
        Comparator.comparing(EmployeeDTO::getEmployeeFirstName, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "departmentName" ->
        Comparator.comparing(EmployeeDTO::getDepartmentName, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "positionName" ->
        Comparator.comparing(EmployeeDTO::getPositionName, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "startDate" ->
        Comparator.comparing(EmployeeDTO::getEmploymentHistoryStartDate, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "salary" ->
        Comparator.comparing(EmployeeDTO::getContractBaseSalary, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      default -> Comparator.comparing(EmployeeDTO::getEmployeeFirstName, Comparator.naturalOrder());
    };
  }
}
