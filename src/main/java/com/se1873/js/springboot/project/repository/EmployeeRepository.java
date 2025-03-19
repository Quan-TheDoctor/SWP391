package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.User;
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
          "where e.isDeleted = false AND (lower(e.firstName) like concat('%',:firstName,'%') or " +
          "lower(e.lastName) like concat('%',:lastName,'%'))")
  Page<Employee> searchEmployee(@Param("firstName") String firstName,@Param("lastName") String lastName,Pageable pageable);

  @Query("SELECT e FROM Employee e " +
          "WHERE (:firstName IS NULL OR lower(e.firstName) LIKE concat('%', :firstName, '%')) " +
          "AND (:lastName IS NULL OR lower(e.lastName) LIKE concat('%', :lastName, '%')) " +
          "AND e.isDeleted = false")
  Page<Employee> searchEmployeebyEmployeeName(@Param("firstName") String firstName,
                                @Param("lastName") String lastName,
                                Pageable pageable);

  @Query("select count(e.employeeId) from Employee e")
  Integer getEmployeeCount();


  List<Employee> findAllByEmployeeIdIn(List<Integer> employeeIds);

  Employee getEmployeeByEmployeeIdAndIsDeleted(Integer employeeId, Boolean isDeleted);

  Employee getEmployeeByUser(User user);

  Employee getEmployeeByUser_UserId(Integer userUserId);
}
