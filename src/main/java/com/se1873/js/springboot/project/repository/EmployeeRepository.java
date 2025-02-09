package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.management.entity.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.employmentHistories")
  List<Employee> findAllWithEmployementHistories(Sort sort);

  @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.contracts")
  List<Employee> findAllWithContracts(Sort sort);

  Employee findEmployeeByEmployeeId(Integer employeeId);
}
