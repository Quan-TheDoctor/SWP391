package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final UserRepository userRepository;
  private final ContractRepository contractRepository;

  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return convertEmployeeToEmployeeDTO(employeeRepository.findByEmployeeId(employeeId));
  }

  public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
    return employeeRepository.findAll(pageable)
      .map(this::convertEmployeeToEmployeeDTO);
  }

  public Page<EmployeeDTO> filter(String field, Integer value, Pageable pageable) {
    Specification<Employee> spec = (root, query, cb) -> {
      // Join với employment history và lọc bản ghi hiện tại
      Join<Employee, EmploymentHistory> employmentHistoryJoin = root.join("employmentHistories");
      Predicate isCurrentPredicate = cb.equal(employmentHistoryJoin.get("isCurrent"), true);


      if (field != null && value != null) {
        switch (field.toLowerCase()) { // Điều kiện filter dựa trên field và value
          case "departmentid":
            Predicate departmentPredicate = cb.equal(employmentHistoryJoin.get("department").get("id"), value);
            return cb.and(isCurrentPredicate, departmentPredicate);
          case "positionid":
            Predicate positionPredicate = cb.equal(employmentHistoryJoin.get("position").get("id"), value);
            return cb.and(isCurrentPredicate, positionPredicate);
          default:
            throw new IllegalArgumentException("Invalid filter field: " + field);
        }
      }
      return isCurrentPredicate;
    };

    return employeeRepository.findAll(spec, pageable)
      .map(this::convertEmployeeToEmployeeDTO);
  }

  public Page<EmployeeDTO> sort(String direction, String field, Pageable pageable) {
    if (!Arrays.asList("name", "code", "department", "position").contains(field.toLowerCase())) {
      throw new IllegalArgumentException("Invalid sort field");
    }

    Sort sort = Sort.by(Sort.Direction.fromString(direction), getSortProperty(field));

    Pageable sortedPageable = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      sort
    );

    Specification<Employee> spec = (root, query, cb) -> {
      Join<Employee, EmploymentHistory> historyJoin = root.join("employmentHistories");
      return cb.equal(historyJoin.get("isCurrent"), true);
    };

    return employeeRepository.findAll(spec, sortedPageable)
      .map(this::convertEmployeeToEmployeeDTO);
  }

  private String getSortProperty(String field) {
    switch (field.toLowerCase()) {
      case "name": return "firstName";
      case "code": return "employeeCode";
      case "department": return "employmentHistories.department.departmentName";
      case "position": return "employmentHistories.position.positionName";
      default: throw new IllegalArgumentException();
    }
  }

  @Transactional
  public void insertEmployee(EmployeeDTO employeeDTO) {
    try {
      log.info("1");

      Employee saveEmployee = employeeDTO.getEmployee();
      if (saveEmployee.getEmployeeId() != null) {
        saveEmployee.setEmployeeId(null);
      }
      log.info("Employee before save: {}", saveEmployee);
      log.info("employeeCode: {}", saveEmployee.getEmployeeCode());
      log.info("idNumber: {}", saveEmployee.getIdNumber());
      log.info("companyEmail: {}", saveEmployee.getCompanyEmail());
      employeeRepository.save(saveEmployee);
      log.info("2");
      Employee employee = employeeRepository.findByEmployeeCode(employeeDTO.getEmployee().getEmployeeCode());
      log.info("3");
      Department department = departmentRepository.findByDepartmentCode(employeeDTO.getEmploymentHistory().getDepartment().getDepartmentCode());
      log.info("4");
      Position position = positionRepository.findByPositionCode(employeeDTO.getEmploymentHistory().getPosition().getPositionCode());
      log.info("5");
      EmploymentHistory employmentHistory = employeeDTO.getEmploymentHistory();
      employmentHistory.setEmployee(employee);
      employmentHistory.setDepartment(department);
      employmentHistory.setPosition(position);
      employmentHistory.setIsCurrent(true);
      Contract contract = employeeDTO.getContract();
      contract.setEmployee(employee);
      contract.setPresent(true);

      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String hashedPassword = passwordEncoder.encode("test");

      User user = User.builder()
        .createdAt(LocalDateTime.now())
        .employee(employee)
        .passwordHash(hashedPassword)
        .username(employeeDTO.getEmployee().getCompanyEmail())
        .build();

      contractRepository.save(contract);
      userRepository.save(user);
      employmentHistoryRepository.save(employmentHistory);

      log.info(employee.getEmployeeId().toString());
      log.info(department.getDepartmentId().toString());
      log.info(position.getPositionId().toString());

    } catch (DataIntegrityViolationException e) {
      log.error("Employee with ID {} already exists. Error: {}", employeeDTO.getEmployee().getEmployeeId(), e.getMessage());
      throw new DataIntegrityViolationException(employeeDTO.toString());
    }
  }

  private EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
    EmploymentHistory employmentHistory = null;
    Department department = null;
    Position position = null;
    Contract contract = null;

    for (var emp : employee.getEmploymentHistories()) {
      if (emp.getIsCurrent()) {
        employmentHistory = emp;
        department = emp.getDepartment();
        position = emp.getPosition();
      }
    }

    for (var cont : employee.getContracts()) {
      if (cont.isPresent()) {
        contract = cont;
      }
    }

    return EmployeeDTO.builder()
      .employee(employee)
      .employmentHistory(employmentHistory)
      .employmentHistories(employee.getEmploymentHistories())
      .qualifications(employee.getQualifications())
      .department(department)
      .position(position)
      .contract(contract)
      .build();
  }

  private EmployeeDTO convertDepartmentToEmployeeDTO(Department department) {
    if (department == null) {
      return null;
    }

    List<Employee> employees = new ArrayList<>();
    List<EmploymentHistory> employmentHistories = department.getEmploymentHistory();

    for (EmploymentHistory history : employmentHistories) {
      if (history.getIsCurrent()) {
        employees.add(history.getEmployee());
      }
    }

    return EmployeeDTO.builder()
      .department(department)
      .employmentHistories(employmentHistories)
      .employee((Employee) employees) // Cần đảm bảo EmployeeDTO có danh sách Employees
      .build();
  }

}
