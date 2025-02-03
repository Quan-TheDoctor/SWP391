package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  @Query("SELECT r FROM Role r " +
    "JOIN DepartmentEmployee de ON de.role.roleId = r.roleId " +
    "JOIN Employee e ON e.employeeId = de.employee.employeeId " +
    "WHERE e.employeeId = :employeeId AND de.isPresent = true")
  Role findRoleByEmployeeId(@Param("employeeId") Integer employeeId);

  Role findRoleByRoleId(Integer roleId);
}
