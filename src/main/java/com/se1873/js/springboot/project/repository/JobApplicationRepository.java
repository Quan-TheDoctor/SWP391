package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobPositionId(Long jobPositionId);
    List<JobApplication> findByStatus(String status);
    List<JobApplication> findByJobPositionIdAndStatus(Long jobPositionId, String status);

    List<JobApplication> findJobApplicationByJobPosition_IdAndStatus(Long id, String status);
}