package com.se1873.js.springboot.project.service.employee.command;

import com.se1873.js.springboot.project.dto.DependentDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.mapper.DependentDTOMapper;
import com.se1873.js.springboot.project.repository.ContractRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.EmploymentHistoryRepository;
import com.se1873.js.springboot.project.service.contract.ContractService;
import com.se1873.js.springboot.project.service.employment_history.EmploymentHistoryService;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeCommandServiceImpl implements EmployeeCommandService {
  private final EmployeeRepository employeeRepository;
  private final UserService userService;
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DependentDTOMapper dependentDTOMapper;
  private final ContractService contractService;
  private final EmploymentHistoryService employmentHistoryService;

  public EmployeeCommandServiceImpl(EmployeeRepository employeeRepository, UserService userService, EmploymentHistoryRepository employmentHistoryRepository, DependentDTOMapper dependentDTOMapper, ContractService contractService, EmploymentHistoryService employmentHistoryService) {
    this.employeeRepository = employeeRepository;
    this.userService = userService;
    this.employmentHistoryRepository = employmentHistoryRepository;
    this.dependentDTOMapper = dependentDTOMapper;
    this.contractService = contractService;
    this.employmentHistoryService = employmentHistoryService;
  }

  @Override
  public void saveEmployee(EmployeeDTO employeeDTO, Department department, Position position) {
    if (employeeDTO.getEmployeeId() == null) {
      createNewEmployeeWithRelatedRecords(employeeDTO, department, position);
    } else {
      updateExistingEmployeeWithRelatedRecords(employeeDTO, department, position);
    }
  }
  private void createNewEmployeeWithRelatedRecords(EmployeeDTO dto, Department department, Position position) {
    Employee employee = createAndSaveBaseEmployee(dto);
    userService.createUserAccount(employee);

    createInitialEmploymentHistory(employee, department, position, dto);
    createInitialContract(employee, dto);

    if (dto.getDependents() != null) {
      handleDependents(employee, dto.getDependents());
    }

    employeeRepository.save(employee);
  }

  private void updateExistingEmployeeWithRelatedRecords(EmployeeDTO dto, Department department, Position position) {
    Employee employee = employeeRepository.getEmployeeByEmployeeId(dto.getEmployeeId());
    updateEmployeeDetails(employee, dto);
    handleEmploymentHistoryChanges(employee, department, position, dto);
    handleContractChanges(employee, dto);

    if (dto.getDependents() != null) {
      handleDependents(employee, dto.getDependents());
    }

    employeeRepository.save(employee);
  }

  private Employee createAndSaveBaseEmployee(EmployeeDTO dto) {
    Employee employee = new Employee();
    updateEmployeeDetails(employee, dto);
    return employeeRepository.save(employee);
  }

  private void updateEmployeeDetails(Employee employee, EmployeeDTO dto) {
    employee.setFirstName(dto.getEmployeeFirstName());
    employee.setLastName(dto.getEmployeeLastName());
    employee.setBirthDate(dto.getEmployeeBirthDate());
    employee.setGender(dto.getEmployeeGender());
    employee.setIdNumber(dto.getEmployeeIdNumber());
    employee.setPermanentAddress(dto.getEmployeePermanentAddress());
    employee.setTemporaryAddress(dto.getEmployeeTemporaryAddress());
    employee.setPersonalEmail(dto.getEmployeePersonalEmail());
    employee.setPhoneNumber(dto.getEmployeePhone());
    employee.setMaritalStatus(dto.getEmployeeMaritalStatus());
    employee.setBankAccount(dto.getEmployeeBankAccount());
    employee.setBankName(dto.getEmployeeBankName());
    employee.setTaxCode(dto.getEmployeeTaxCode());
    employee.setEmployeeCode(dto.getEmployeeCode());
    employee.setCompanyEmail(dto.getEmployeeCompanyEmail());
    employee.setPicture(dto.getPicture());
    employee.setIsDeleted(false);
  }
  private void createInitialEmploymentHistory(Employee employee, Department department, Position position, EmployeeDTO dto) {
    EmploymentHistory history = EmploymentHistory.builder()
      .employee(employee)
      .department(department)
      .position(position)
      .startDate(dto.getEmploymentHistoryStartDate())
      .endDate(dto.getEmploymentHistoryEndDate())
      .isCurrent(true)
      .build();
    employmentHistoryService.save(history);
  }

  private void handleEmploymentHistoryChanges(Employee employee, Department department, Position position, EmployeeDTO dto) {
    EmploymentHistory currentHistory = employmentHistoryService.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employee.getEmployeeId(), true);

    if (currentHistory == null) {
      createInitialEmploymentHistory(employee, department, position, dto);
      return;
    }

    if (shouldUpdateEmploymentHistory(currentHistory, department, position)) {
      closeCurrentEmploymentHistory(currentHistory);
      createNewEmploymentHistory(employee, department, position);
    }
  }

  private boolean shouldUpdateEmploymentHistory(EmploymentHistory current, Department newDept, Position newPos) {
    return !current.getDepartment().getDepartmentId().equals(newDept.getDepartmentId()) ||
      !current.getPosition().getPositionId().equals(newPos.getPositionId());
  }

  private void closeCurrentEmploymentHistory(EmploymentHistory history) {
    history.setIsCurrent(false);
    history.setEndDate(LocalDate.now());
    employmentHistoryService.save(history);
  }

  private void createNewEmploymentHistory(Employee employee, Department department, Position position) {
    EmploymentHistory newHistory = EmploymentHistory.builder()
      .employee(employee)
      .department(department)
      .position(position)
      .startDate(LocalDate.now())
      .isCurrent(true)
      .build();
    employmentHistoryService.save(newHistory);
  }

  private void createInitialContract(Employee employee, EmployeeDTO dto) {
    Contract contract = Contract.builder()
      .employee(employee)
      .contractType(dto.getContractType())
      .contractCode(dto.getContractCode())
      .startDate(dto.getContractStartDate())
      .endDate(dto.getContractEndDate())
      .baseSalary(dto.getContractBaseSalary() * 1000000)
      .signDate(dto.getContractSignDate())
      .isPresent(true)
      .build();
    contractService.save(contract);
  }

  private void handleContractChanges(Employee employee, EmployeeDTO dto) {
    Contract currentContract = contractService.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);

    if (currentContract == null) {
      createInitialContract(employee, dto);
      return;
    }

    if (isNewContractNeeded(currentContract, dto)) {
      closeCurrentContract(currentContract);
      createInitialContract(employee, dto);
    } else {
      updateExistingContract(currentContract, dto);
    }
  }

  private boolean isNewContractNeeded(Contract current, EmployeeDTO dto) {
    return !current.getContractCode().equals(dto.getContractCode());
  }

  private void closeCurrentContract(Contract contract) {
    contract.setPresent(false);
    contractService.save(contract);
  }

  private void updateExistingContract(Contract contract, EmployeeDTO dto) {
    contract.setContractCode(dto.getContractCode());
    contract.setContractType(dto.getContractType());
    contract.setStartDate(dto.getContractStartDate());
    contract.setEndDate(dto.getContractEndDate());
    contract.setBaseSalary(dto.getContractBaseSalary() * 1000000);
    contract.setSignDate(dto.getContractSignDate());
    contractService.save(contract);
  }

  private void handleDependents(Employee employee, List<DependentDTO> dependentDTOs) {
    if (employee.getDependents() == null) {
      employee.setDependents(new ArrayList<>());
    }

    if (dependentDTOs == null || dependentDTOs.isEmpty()) {
      return;
    }

    List<DependentDTO> validDependents = dependentDTOs.stream()
      .filter(dep -> dep != null && dep.getFullName() != null && !dep.getFullName().isEmpty())
      .collect(Collectors.toList());

    if (validDependents.isEmpty()) {
      return;
    }

    if (!employee.getDependents().isEmpty()) {
      Map<Integer, Dependent> existingDependentsMap = employee.getDependents().stream()
        .collect(Collectors.toMap(Dependent::getDependentId, Function.identity(), (a, b) -> a));

      for (DependentDTO dependentDTO : validDependents) {
        if (dependentDTO.getDependentId() != null && existingDependentsMap.containsKey(dependentDTO.getDependentId())) {
          Dependent existingDependent = existingDependentsMap.get(dependentDTO.getDependentId());
          updateDependentDetails(existingDependent, dependentDTOMapper.toEntity(dependentDTO));
          existingDependentsMap.remove(dependentDTO.getDependentId());
        } else {
          createNewDependent(employee, dependentDTOMapper.toEntity(dependentDTO));
        }
      }
    } else {
      for (DependentDTO dependentDTO : validDependents) {
        createNewDependent(employee, dependentDTOMapper.toEntity(dependentDTO));
      }
    }
  }

  private void createNewDependent(Employee employee, Dependent dependentDTO) {
    Dependent dependent = new Dependent();
    updateDependentDetails(dependent, dependentDTO);
    dependent.setEmployee(employee);

    employee.getDependents().add(dependent);
  }

  private void updateDependentDetails(Dependent dependent, Dependent dependentDTO) {
    dependent.setFullName(dependentDTO.getFullName());
    dependent.setRelationship(dependentDTO.getRelationship());
    dependent.setBirthDate(dependentDTO.getBirthDate());
    dependent.setIdNumber(dependentDTO.getIdNumber());
    dependent.setIsTaxDependent(dependentDTO.getIsTaxDependent());
  }

  @Override
  public void deleteEmployee(Integer employeeId) {
    Employee employee = employeeRepository.getEmployeeByEmployeeId(employeeId);
    if (employee != null) {
      employee.setIsDeleted(true);
      employeeRepository.save(employee);
    }
  }
}
