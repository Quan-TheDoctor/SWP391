package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final ContractRepository contractRepository;
  private final EmployeeRepository employeeRepository;

  public Page<EmployeeDTO> getAll(Pageable pageable) {
    var employees = employeeRepository.findAll(pageable).map(this::convertEmployeeToDTO);
    return employees;
  }

  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return convertEmployeeToDTO(employeeRepository.getEmployeeByEmployeeId(employeeId));
  }

  public void saveEmployee(EmployeeDTO employeeDTO) {
    Department department = departmentRepository.findDepartmentByDepartmentId(employeeDTO.getDepartmentId());
    Position position = positionRepository.findPositionByPositionId(employeeDTO.getPositionId());
    if (employeeDTO.getEmployeeId() == null) {
      Employee employee = new Employee();
      editEmployee(employee, employeeDTO);
      log.info(employeeDTO.toString());
      log.info(employee.toString());
      employee = employeeRepository.save(employee);
      employmentHistoryRepository.save(
        EmploymentHistory.builder()
          .employee(employee)
          .department(department)
          .position(position)
          .startDate(employeeDTO.getEmploymentHistoryStartDate())
          .endDate(employeeDTO.getEmploymentHistoryEndDate())
          .isCurrent(true)
          .build()
      );
      contractRepository.save(
        Contract.builder()
          .employee(employee)
          .contractCode(employeeDTO.getContractCode())
          .contractType(employeeDTO.getContractType())
          .startDate(employeeDTO.getContractStartDate())
          .endDate(employeeDTO.getContractEndDate())
          .baseSalary(employeeDTO.getContractBaseSalary())
          .signDate(employeeDTO.getContractSignDate())
          .isPresent(true)
          .build()
      );
    } else {
      Employee employee = employeeRepository.getEmployeeByEmployeeId(employeeDTO.getEmployeeId());
      editEmployee(employee, employeeDTO);
      employeeRepository.save(employee);

      EmploymentHistory employmentHistory =
        employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employeeDTO.getEmployeeId(), true);

      if(!employmentHistory.getPosition().getPositionId().equals(employeeDTO.getPositionId())
        || !employmentHistory.getDepartment().getDepartmentId().equals(department.getDepartmentId())) {
        Department newDepartment = departmentRepository.findDepartmentByDepartmentId(employeeDTO.getDepartmentId());
        Position newPosition = positionRepository.findPositionByPositionId(employeeDTO.getPositionId());

        employmentHistory.setIsCurrent(false);
        employmentHistory.setEndDate(LocalDate.now());
        employmentHistoryRepository.save(employmentHistory);

        EmploymentHistory newEmploymentHistory = EmploymentHistory.builder()
          .employee(employee)
          .department(newDepartment)
          .position(newPosition)
          .startDate(LocalDate.now())
          .isCurrent(true)
          .build();
        employmentHistoryRepository.save(newEmploymentHistory);
      }

      Contract contract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employeeDTO.getEmployeeId(), true);

      if(contract.getContractCode().equals(employeeDTO.getContractCode())) {
        contract.setEmployee(employee);
        contract.setContractCode(employeeDTO.getContractCode());
        contract.setContractType(employeeDTO.getContractType());
        contract.setStartDate(employeeDTO.getContractStartDate());
        contract.setEndDate(employeeDTO.getContractEndDate());
        contract.setBaseSalary(employeeDTO.getContractBaseSalary());
        contract.setSignDate(employeeDTO.getContractSignDate());
        contract.setPresent(true);
        contractRepository.save(contract);
      } else {
        contract.setPresent(false);
        contractRepository.save(contract);

        Contract newContract = new Contract();
        newContract.setEmployee(employee);
        newContract.setContractCode(employeeDTO.getContractCode());
        newContract.setContractType(employeeDTO.getContractType());
        newContract.setStartDate(employeeDTO.getContractStartDate());
        newContract.setEndDate(employeeDTO.getContractEndDate());
        newContract.setBaseSalary(employeeDTO.getContractBaseSalary());
        newContract.setSignDate(employeeDTO.getContractSignDate());
        newContract.setPresent(true);
        contractRepository.save(newContract);
      }

    }

  }

  private Employee editEmployee(Employee employee, EmployeeDTO employeeDTO) {
    employee.setFirstName(employeeDTO.getEmployeeFirstName());
    employee.setLastName(employeeDTO.getEmployeeLastName());
    employee.setBirthDate(employeeDTO.getEmployeeBirthDate());
    employee.setGender(employeeDTO.getEmployeeGender());
    employee.setIdNumber(employeeDTO.getEmployeeIdNumber());
    employee.setPermanentAddress(employeeDTO.getEmployeePermanentAddress());
    employee.setTemporaryAddress(employeeDTO.getEmployeeTemporaryAddress());
    employee.setPersonalEmail(employeeDTO.getEmployeePersonalEmail());
    employee.setPhoneNumber(employeeDTO.getEmployeePhone());
    employee.setMaritalStatus(employeeDTO.getEmployeeMaritalStatus());
    employee.setBankAccount(employeeDTO.getEmployeeBankAccount());
    employee.setBankName(employeeDTO.getEmployeeBankName());
    employee.setTaxCode(employeeDTO.getEmployeeTaxCode());
    employee.setEmployeeCode(employeeDTO.getEmployeeCode());
    employee.setCompanyEmail(employeeDTO.getEmployeeCompanyEmail());

    return employee;
  }

  public Page<EmployeeDTO> filterByField(String field,String value,Pageable pageable){
    Page<Employee> employees = Page.empty();
    switch (field){
      case "department":
        employees = employeeRepository.findEmployeesByDepartmentName(value,pageable);
        break;
      case "position":
        employees = employeeRepository.findEmployeesByPositionName(value,pageable);
        break;
      default:
        throw new IllegalArgumentException("not found");
    }

    return employees.map(this :: convertEmployeeToDTO);
  }

  private EmployeeDTO convertEmployeeToDTO(Employee employee) {
    EmploymentHistory employmentHistory =
      employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employee.getEmployeeId(), true);
    Department department = departmentRepository.findDepartmentByDepartmentId(employmentHistory.getDepartment().getDepartmentId());
    Position position = positionRepository.findPositionByPositionId(employmentHistory.getPosition().getPositionId());
    Contract contract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);

    return EmployeeDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeCode(employee.getEmployeeCode())
      .employeeFirstName(employee.getFirstName())
      .employeeLastName(employee.getLastName())
      .employeeBirthDate(employee.getBirthDate())
      .employeeGender(employee.getGender())
      .employeeIdNumber(employee.getIdNumber())
      .employeePermanentAddress(employee.getPermanentAddress())
      .employeeTemporaryAddress(employee.getTemporaryAddress())
      .employeePersonalEmail(employee.getPersonalEmail())
      .employeeCompanyEmail(employee.getCompanyEmail())
      .employeePhone(employee.getPhoneNumber())
      .employeeMaritalStatus(employee.getMaritalStatus())
      .employeeBankAccount(employee.getBankAccount())
      .employeeBankName(employee.getBankName())
      .employeeTaxCode(employee.getTaxCode())

      .departmentId(department.getDepartmentId())
      .departmentName(department.getDepartmentName())

      .positionId(position.getPositionId())
      .positionName(position.getPositionName())

      .employmentHistoryId(employmentHistory.getHistoryId())
      .employmentHistoryStartDate(employmentHistory.getStartDate())
      .employmentHistoryEndDate(employmentHistory.getEndDate())
      .employmentHistoryIsCurrent(employmentHistory.getIsCurrent())

      .contractId(contract.getContractId())
      .contractType(contract.getContractType())
      .contractCode(contract.getContractCode())
      .contractStartDate(contract.getStartDate())
      .contractEndDate(contract.getEndDate())
      .contractBaseSalary(contract.getBaseSalary())
      .contractSignDate(contract.getSignDate())
      .build();
  }
}
