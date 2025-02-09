package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Dependent;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {
  Set<Dependent> findAllByEmployee_EmployeeId(Integer employeeEmployeeId, Sort sort);
}
