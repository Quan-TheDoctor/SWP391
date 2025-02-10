package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

  public List<EmployeeDTO> getAllEmployees() {
    return employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "employeeId"))
      .stream()
      .map(this::convertEmployeeToEmployeeDTO)
      .toList();
  }

  public List<EmployeeDTO> filter(Integer field){
    for(var i : employmentHistoryRepository.finds(field)) {
      log.info(i.getHistoryId().toString());
    }

    return null;
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
    //employeeRepository.saveAndFlush(employeeDTO.getEmployee());
  }

  private EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
    EmploymentHistory employmentHistory = null;
    Department department = null;
    Position position = null;
    Contract contract = null;

    for(var emp : employee.getEmploymentHistories()) {
      if(emp.getIsCurrent()) {
        employmentHistory = emp;
        department = emp.getDepartment();
        position = emp.getPosition();
      }
    }

    for(var cont : employee.getContracts()) {
      if(cont.isPresent()) {
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
}
