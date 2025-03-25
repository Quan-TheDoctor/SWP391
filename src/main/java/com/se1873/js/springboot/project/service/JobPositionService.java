package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.JobPositionDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.JobPosition;
import com.se1873.js.springboot.project.repository.JobPositionRepository;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPositionService {
    private final JobPositionRepository jobPositionRepository;
    private final DepartmentService departmentService;

    public List<JobPositionDTO> getAllPositions() {
        return jobPositionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobPositionDTO> getOpenPositions() {
        return jobPositionRepository.findByStatus("OPEN").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobPositionDTO> getPositionsByDepartment(String department) {
        return jobPositionRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public JobPositionDTO getPositionById(Long id) {
        return jobPositionRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<String> getAllDepartments() {
        return departmentService.getAllDepartments().stream()
                .map(Department::getDepartmentName)
                .collect(Collectors.toList());
    }

    public JobPositionDTO createPosition(JobPositionDTO positionDTO) {
        JobPosition position = new JobPosition();
        position.setTitle(positionDTO.getTitle());
        position.setDepartment(positionDTO.getDepartment());
        position.setNumberOfPositions(positionDTO.getNumberOfPositions());
        position.setDeadline(positionDTO.getDeadline());
        position.setDescription(positionDTO.getDescription());
        position.setRequirements(positionDTO.getRequirements());
        position.setResponsibilities(positionDTO.getResponsibilities());
        position.setSalaryRange(positionDTO.getSalaryRange());
        position.setStatus("OPEN");

        JobPosition savedPosition = jobPositionRepository.save(position);
        return convertToDTO(savedPosition);
    }

    private JobPositionDTO convertToDTO(JobPosition position) {
        JobPositionDTO dto = new JobPositionDTO();
        dto.setId(position.getId());
        dto.setTitle(position.getTitle());
        dto.setDepartment(position.getDepartment());
        dto.setNumberOfPositions(position.getNumberOfPositions());
        dto.setDeadline(position.getDeadline());
        dto.setDescription(position.getDescription());
        dto.setRequirements(position.getRequirements());
        dto.setResponsibilities(position.getResponsibilities());
        dto.setSalaryRange(position.getSalaryRange());
        dto.setStatus(position.getStatus());
        return dto;
    }
} 