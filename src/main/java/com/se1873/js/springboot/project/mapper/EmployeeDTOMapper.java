package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(
  componentModel = "spring",
  uses = {
    DepartmentDTOMapper.class,
    PositionDTOMapper.class,
    EmploymentHistoryDTOMapper.class,
    ContractDTOMapper.class,
    DependentDTOMapper.class
  }
)
public abstract class EmployeeDTOMapper {
  @Autowired
  private DependentDTOMapper dependentDTOMapper;

  @Mapping(target = "departmentId", ignore = true)
  @Mapping(target = "departmentName", ignore = true)
  @Mapping(target = "departmentDescription", ignore = true)
  @Mapping(target = "departmentCode", ignore = true)
  @Mapping(target = "departmentCreatedAt", ignore = true)

  @Mapping(target = "positionId", ignore = true)
  @Mapping(target = "positionName", ignore = true)
  @Mapping(target = "positionDescription", ignore = true)
  @Mapping(target = "positionCode", ignore = true)
  @Mapping(target = "positionLevel", ignore = true)
  @Mapping(target = "positionCreatedAt", ignore = true)

  @Mapping(target = "employmentHistoryId", ignore = true)
  @Mapping(target = "employmentHistoryStartDate", ignore = true)
  @Mapping(target = "employmentHistoryEndDate", ignore = true)
  @Mapping(target = "employmentHistoryIsCurrent", ignore = true)
  @Mapping(target = "transferReason", ignore = true)
  @Mapping(target = "decisionNumber", ignore = true)
  @Mapping(target = "employmentHistoryCreatedAt", ignore = true)

  @Mapping(target = "contractId", ignore = true)
  @Mapping(target = "contractType", ignore = true)
  @Mapping(target = "contractCode", ignore = true)
  @Mapping(target = "contractStartDate", ignore = true)
  @Mapping(target = "contractEndDate", ignore = true)
  @Mapping(target = "contractBaseSalary", ignore = true)
  @Mapping(target = "contractSignDate", ignore = true)
  @Mapping(target = "contractIsPresent", ignore = true)
  @Mapping(target = "contractCreatedAt", ignore = true)

  @Mapping(target = "dependents", ignore = true)
  public abstract EmployeeDTO toDTO(Employee employee);

  @AfterMapping
  protected void mapNestedFields(
    Employee employee,
    @MappingTarget EmployeeDTO employeeDTO
  ) {
    employeeDTO.setEmployeeId(employee.getEmployeeId());
    employeeDTO.setEmployeeCode(employee.getEmployeeCode());
    employeeDTO.setEmployeeFirstName(employee.getFirstName());
    employeeDTO.setEmployeeLastName(employee.getLastName());
    employeeDTO.setEmployeeGender(employee.getGender());
    employeeDTO.setEmployeeBirthDate(employee.getBirthDate());
    employeeDTO.setEmployeeBankAccount(employee.getBankAccount());
    employeeDTO.setEmployeeBankName(employee.getBankName());
    employeeDTO.setEmployeeTemporaryAddress(employee.getTemporaryAddress());
    employeeDTO.setEmployeePermanentAddress(employee.getPermanentAddress());
    employeeDTO.setEmployeeIdNumber(employee.getIdNumber());
    employeeDTO.setEmployeeTaxCode(employee.getTaxCode());
    employeeDTO.setEmployeePersonalEmail(employee.getPersonalEmail());
    employeeDTO.setEmployeeCompanyEmail(employee.getCompanyEmail());
    employeeDTO.setEmployeePhone(employee.getPhoneNumber());
    employeeDTO.setEmployeeMaritalStatus(employee.getMaritalStatus());
    employeeDTO.setPicture(employee.getPicture());

    EmploymentHistory currentEmploymentHistory = getCurrentEmploymentHistory(employee);
    if (currentEmploymentHistory != null) {
      mapEmploymentHistory(currentEmploymentHistory, employeeDTO);
    }

    Contract currentContract = getCurrentContract(employee);
    if (currentContract != null) {
      mapContract(currentContract, employeeDTO);
    }

    if (employee.getDependents() != null && !employee.getDependents().isEmpty()) {
      List<DependentDTO> dependentDTOs = employee.getDependents().stream()
        .map(dependentDTOMapper::toDTO)
        .collect(Collectors.toList());
      employeeDTO.setDependents(dependentDTOs);
    } else {
      employeeDTO.setDependents(new ArrayList<>());
    }
  }

  private void mapEmploymentHistory(EmploymentHistory history, EmployeeDTO dto) {
    dto.setEmploymentHistoryId(history.getHistoryId());
    dto.setEmploymentHistoryStartDate(history.getStartDate());
    dto.setEmploymentHistoryEndDate(history.getEndDate());
    dto.setEmploymentHistoryIsCurrent(history.getIsCurrent());
    dto.setTransferReason(history.getTransferReason());
    dto.setDecisionNumber(history.getDecisionNumber());
    dto.setEmploymentHistoryCreatedAt(history.getCreatedAt());

    if (history.getDepartment() != null) {
      Department department = history.getDepartment();
      dto.setDepartmentId(department.getDepartmentId());
      dto.setDepartmentName(department.getDepartmentName());
      dto.setDepartmentDescription(department.getDescription());
      dto.setDepartmentCode(department.getDepartmentCode());
      dto.setDepartmentCreatedAt(department.getCreatedAt());
    }

    if (history.getPosition() != null) {
      Position position = history.getPosition();
      dto.setPositionId(position.getPositionId());
      dto.setPositionName(position.getPositionName());
      dto.setPositionDescription(position.getDescription());
      dto.setPositionCode(position.getPositionCode());
      dto.setPositionLevel(position.getLevel());
      dto.setPositionCreatedAt(position.getCreatedAt());
    }
  }

  private void mapContract(Contract contract, EmployeeDTO dto) {
    dto.setContractId(contract.getContractId());
    dto.setContractType(contract.getContractType());
    dto.setContractCode(contract.getContractCode());
    dto.setContractStartDate(contract.getStartDate());
    dto.setContractEndDate(contract.getEndDate());
    dto.setContractBaseSalary(contract.getBaseSalary());
    dto.setContractSignDate(contract.getSignDate());
    dto.setContractIsPresent(contract.isPresent());
    dto.setContractCreatedAt(contract.getCreatedAt());
  }

  private EmploymentHistory getCurrentEmploymentHistory(Employee employee) {
    if (employee.getEmploymentHistories() == null) return null;
    return employee.getEmploymentHistories().stream()
      .filter(EmploymentHistory::getIsCurrent)
      .findFirst()
      .orElse(null);
  }

  private Contract getCurrentContract(Employee employee) {
    if (employee.getContracts() == null) return null;
    return employee.getContracts().stream()
      .filter(Contract::isPresent)
      .findFirst()
      .orElse(null);
  }
}
