package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.JobPositionDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.JobPosition;
import com.se1873.js.springboot.project.repository.JobApplicationRepository;
import com.se1873.js.springboot.project.repository.JobPositionRepository;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPositionService {
    private final JobPositionRepository jobPositionRepository;
    private final DepartmentService departmentService;
    private final JobApplicationRepository jobApplicationRepository;

    public List<JobPositionDTO> getAllPositions() {
        return jobPositionRepository.findByIsDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobPositionDTO> getOpenPositions() {
        return jobPositionRepository.findByStatusAndIsDeletedFalse("OPEN").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobPositionDTO> getPositionsByDepartment(String department) {
        return jobPositionRepository.findByDepartmentAndIsDeletedFalse(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public JobPositionDTO getPositionById(Long id) {
        return jobPositionRepository.findByIdAndIsDeletedFalse(id)
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

    public JobPositionDTO updatePosition(JobPositionDTO positionDTO) {
        JobPosition position = jobPositionRepository.findById(positionDTO.getId())
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + positionDTO.getId()));
        
        position.setTitle(positionDTO.getTitle());
        position.setDepartment(positionDTO.getDepartment());
        position.setDescription(positionDTO.getDescription());
        position.setRequirements(positionDTO.getRequirements());
        position.setStatus(positionDTO.getStatus());
        position.setPostedDate(positionDTO.getPostedDate());
        position.setDeadline(positionDTO.getDeadline());
        position.setNumberOfPositions(positionDTO.getNumberOfPositions());
        position.setResponsibilities(positionDTO.getResponsibilities());
        position.setSalaryRange(positionDTO.getSalaryRange());
        
        JobPosition updatedPosition = jobPositionRepository.save(position);
        return convertToDTO(updatedPosition);
    }

    public void deletePosition(Long id) {
        JobPosition position = jobPositionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + id));
        
        position.setDeleted(true);
        jobPositionRepository.save(position);
    }

    public JobPositionDTO closePosition(Long id) {
        JobPosition position = jobPositionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + id));
        
        position.setStatus("CLOSED");
        JobPosition updatedPosition = jobPositionRepository.save(position);
        return convertToDTO(updatedPosition);
    }

    public List<JobPositionDTO> searchPositions(String keyword) {
        return jobPositionRepository.findByTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCase(keyword, keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobPositionDTO> getPositionsByStatus(String status) {
        return jobPositionRepository.findByStatusAndIsDeletedFalse(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<Long, Long> getApplicationCounts() {
        return jobApplicationRepository.countApplicationsByPositionId();
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
        dto.setDeleted(position.isDeleted());
        dto.setPostedDate(position.getPostedDate());
        return dto;
    }
} 