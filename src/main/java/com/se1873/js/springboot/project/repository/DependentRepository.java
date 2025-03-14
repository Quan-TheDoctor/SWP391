package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {

  @Query("SELECT COUNT(d.dependentId) FROM SalaryRecord s " +
    "LEFT JOIN Dependent d ON s.employee.employeeId = d.employee.employeeId " +
    "WHERE s.salaryId = ?1")
  Integer getNumberOfDependentsBySalaryID(Integer salaryId);

  List<Dependent> findByEmployee_EmployeeId(Integer employeeEmployeeId);

  void deleteByDependentId(Integer dependentId);
}
