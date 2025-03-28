package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.JobApplicationDTO;
import com.se1873.js.springboot.project.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobPositionId(Long positionId);
    List<JobApplication> findByStatus(String status);
    List<JobApplication> findByJobPositionIdAndStatus(Long positionId, String status);

    List<JobApplication> findJobApplicationByJobPosition_IdAndStatus(Long id, String status);

    @Query("SELECT j.jobPosition.id as positionId, COUNT(j) as count FROM JobApplication j GROUP BY j.jobPosition.id")
    Map<Long, Long> countApplicationsByPositionId();

  JobApplicationDTO getJobApplicationById(Long id);
}