package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByStatus(String status);
    List<JobPosition> findByDepartment(String department);
    List<JobPosition> findByStatusAndIsDeletedFalse(String status);
    List<JobPosition> findByDepartmentAndIsDeletedFalse(String department);
    List<JobPosition> findByIsDeletedFalse();
    Optional<JobPosition> findByIdAndIsDeletedFalse(Long id);
    List<JobPosition> findByTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCase(String title, String department);
} 