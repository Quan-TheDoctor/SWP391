package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByStatus(String status);
    List<JobPosition> findByDepartment(String department);
} 