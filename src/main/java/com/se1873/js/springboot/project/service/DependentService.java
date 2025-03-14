package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.DependentDTO;
import com.se1873.js.springboot.project.entity.Dependent;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.mapper.DependentDTOMapper;
import com.se1873.js.springboot.project.repository.DependentRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DependentService {
  private final DependentRepository dependentRepository;
  private final DependentDTOMapper dependentDTOMapper;
  private final EmployeeRepository employeeRepository;

  public List<DependentDTO> getDependentsByEmployeeId(Integer employeeId) {
    List<Dependent> dependents = dependentRepository.findByEmployee_EmployeeId(employeeId);
    return dependents.stream()
      .map(dependentDTOMapper::toDTO)
      .collect(Collectors.toList());
  }

  public DependentDTO getDependentById(Long id) {
    Dependent dependent = dependentRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Dependent not found with id: " + id));
    return dependentDTOMapper.toDTO(dependent);
  }

  @Transactional
  public DependentDTO createDependent(DependentDTO dependentDTO) {
    Employee employee = employeeRepository.findEmployeeByEmployeeId(dependentDTO.getEmployeeId());

    Dependent dependent = dependentDTOMapper.toEntity(dependentDTO);
    dependent.setEmployee(employee);

    Dependent savedDependent = dependentRepository.save(dependent);
    return dependentDTOMapper.toDTO(savedDependent);
  }

  @Transactional
  public DependentDTO updateDependent(Long id, DependentDTO dependentDTO) {
    Dependent existingDependent = dependentRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Dependent not found with id: " + id));

    existingDependent.setFullName(dependentDTO.getFullName());
    existingDependent.setRelationship(dependentDTO.getRelationship());
    existingDependent.setBirthDate(dependentDTO.getBirthDate());
    existingDependent.setIdNumber(dependentDTO.getIdNumber());
    existingDependent.setIsTaxDependent(dependentDTO.getIsTaxDependent());

    Dependent updatedDependent = dependentRepository.save(existingDependent);
    return dependentDTOMapper.toDTO(updatedDependent);
  }

  @Transactional
  public void deleteDependent(Integer id) {
    dependentRepository.deleteByDependentId(id);
  }

  @Transactional
  public void saveDependents(Long employeeId, List<DependentDTO> dependentDTOs) {
    Employee employee = employeeRepository.findById(employeeId)
      .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

    employee.getDependents().clear();

    if (dependentDTOs != null && !dependentDTOs.isEmpty()) {
      for (DependentDTO dto : dependentDTOs) {
        Dependent dependent = dependentDTOMapper.toEntity(dto);
        dependent.setEmployee(employee);
        employee.getDependents().add(dependent);
      }
    }

    employeeRepository.save(employee);
  }
}
