package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, Long> {
  List<Qualification> findAllByEmployee(Employee employee);
}
