package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.component.EntityRelationshipHandler;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {
  private final EmployeeRepository employeeRepository;
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final ContractRepository contractRepository;
  private final DependentRepository dependentRepository;
  private final QualificationRepository qualificationRepository;
  private final EmployeeSkillRepository employeeSkillRepository;

  private final EntityRelationshipHandler entityRelationshipHandler;
  private final EntityManager entityManager;

  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return convertToEmployeeDTO(employeeRepository.findEmployeeByEmployeeId(employeeId));
  }

  public List<EmployeeDTO> getAllEmployees() {
    return employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "employeeId"))
      .stream()
      .map(this::convertToEmployeeDTO)
      .toList();
  }

  private EmployeeDTO convertToEmployeeDTO(Employee employee) {
    EmploymentHistory currentEmploymentHistory = employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employee.getEmployeeId(), true);
    Department department = departmentRepository.findDepartmentByDepartmentId(currentEmploymentHistory.getDepartment().getDepartmentId());
    Position position = positionRepository.findPositionByPositionId(currentEmploymentHistory.getPosition().getPositionId());
    Contract currentContract = contractRepository.findContractByEmployee_EmployeeIdAndIsPresent(employee.getEmployeeId(), true);
    Set<Dependent> dependents = dependentRepository.findAllByEmployee_EmployeeId(employee.getEmployeeId(), Sort.by(Sort.Direction.ASC, "dependentId"));
    List<Qualification> qualifications = qualificationRepository.findAllByEmployee(employee);
    List<EmployeeSkill> skills = employeeSkillRepository.findAllByEmployee(employee);
    return EmployeeDTO
      .builder()
      .employee(employee)
      .department(department)
      .position(position)
      .currentEmploymentHistory(currentEmploymentHistory)
      .currentContract(currentContract)
      .dependents(dependents)
      .qualifications(qualifications)
      .employeeSkills(skills)
      .build();
  }

  @Transactional
  public void updateEmployee(EmployeeDTO employeeDTO) {
    try {
      Employee existingEmployee = employeeRepository.findByEmployeeId(employeeDTO.getEmployee().getEmployeeId());
      if(existingEmployee == null) {
        throw new EntityNotFoundException("Employee not found");
      }
      entityManager.refresh(existingEmployee);

      Set<Dependent> currentDependents = new HashSet<>(existingEmployee.getDependents());

      Employee updatedEmployee = convertToEmployee(employeeDTO);

      handleDependents(currentDependents, employeeDTO.getDependents(), updatedEmployee);

      employeeRepository.save(updatedEmployee);
      entityManager.flush();
    } catch (ConcurrentModificationException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
        "Data đã bị tác động. Refresh và thử lại", e);
    }
  }
  private void handleDependents(Set<Dependent> currentDependents,
                                Set<Dependent> newDependents,
                                Employee employee) {
    Set<Dependent> dependentsToRemove = new HashSet<>(currentDependents);
    dependentsToRemove.removeAll(newDependents);

    dependentsToRemove.forEach(dependent -> {
      dependent.setEmployee(null);
      dependentRepository.delete(dependent);
    });
    entityRelationshipHandler.setRelationships(newDependents, employee, Dependent::setEmployee);
  }

  private Employee convertToEmployee(EmployeeDTO employeeDTO) {
    return employeeDTO.getEmployee();
  }

  private void saveRelatedEntities(Employee employee, EmployeeDTO employeeDTO) {
    if (employeeDTO.getDependents() != null) {
      dependentRepository.saveAll(employeeDTO.getDependents());
    }
    employeeRepository.save(employee);
  }

//  private Comparator<Employee> comparator(String field, String direction) {
//    var flag = "asc".equalsIgnoreCase(direction);
//
//    Function<Employee, Comparable> fieldsMap = fieldsMap(field);
//    if (fieldsMap == null) throw new IllegalArgumentException("Unknown field: " + field);
//    Comparator<Employee> comparator = Comparator.comparing(
//      fieldsMap,
//      (val1, val2) -> {
//        if (val1 == null && val2 == null) return 0;
//        if (val1 == null) return 1;
//        if (val2 == null) return -1;
//        return (val1).compareTo(val2);
//      }
////      flag ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsLast(Comparator.reverseOrder())
//    );
//    comparator = flag ? comparator : comparator.reversed();
//    return comparator;
//  }
//
//  private Function<Employee, Comparable> fieldsMap(String field) {
//    Map<String, Function<Employee, Comparable>> fieldsMap = new HashMap<>();
//    fieldsMap.put("employeeCode", Employee::getEmployeeId);
//    return fieldsMap.get(field);
//  }
}
