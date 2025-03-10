package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Employee getEmployeeByEmployeeId(Integer employeeId);

  Employee findEmployeeByEmployeeId(Integer employeeId);

//  @Query("SELECT e FROM Employee e " +
//          "JOIN EmploymentHistory eh ON e.employeeId = eh.employee.employeeId " +
//          "JOIN Department d ON eh.department.departmentId = d.departmentId " +
//          "where d.departmentName = :departmentName and eh.isCurrent = true")
  @Query("SELECT e FROM Employee e " +
          "JOIN e.employmentHistories eh " +
          "JOIN eh.department d " +
          "where d.departmentName = :departmentName and eh.isCurrent = true")
  Page<Employee> findEmployeesByDepartmentName(@Param("departmentName") String department, Pageable pageable);


  @Query("select e from Employee e " +
          "join e.employmentHistories eh " +
          "join eh.position p " +
          "where p.positionName = :positionName and eh.isCurrent = true")
  Page<Employee> findEmployeesByPositionName(@Param("positionName") String position, Pageable pageable);

  Page<Employee> findAll(Pageable pageable);

  @Query("select e from Employee e " +
          "where lower(e.firstName) like concat('%',:firstName,'%') or " +
          "lower(e.lastName) like concat('%',:lastName,'%')")
  Page<Employee> searchEmployee(@Param("firstName") String firstName,@Param("lastName") String lastName,Pageable pageable);


}
