package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> , JpaSpecificationExecutor<Employee> {
  Employee findByEmployeeId(Integer employeeId);

  Employee findByEmployeeCode(String employeeCode);
  @Query("SELECT e FROM Employee e " +
          "JOIN EmploymentHistory eh ON eh.employee = e " +
          "JOIN Department d ON d = eh.department " +
          "WHERE eh.isCurrent = true " +
          "ORDER BY d.departmentName ASC")

  List<Employee> sortByDepartment(Pageable pageable);

  @Query("SELECT e FROM Employee e WHERE lower(concat(e.firstName, ' ', e.lastName)) like lower(concat('%', replace(:Name, ' ', '%'), '%'))")
  Page<Employee> searchEmployeesByFirstNameAndLastName(@Param("Name") String Name, Pageable pageable);

}
