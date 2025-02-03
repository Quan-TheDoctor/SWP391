package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PositionRepository extends JpaRepository<Position, Long> {
  @Query("SELECT p FROM Position p " +
    "JOIN DepartmentEmployee de ON de.position.positionId = p.positionId " +
    "JOIN Employee e ON e.employeeId = de.employee.employeeId " +
    "WHERE e.employeeId = :employeeId AND de.isPresent = true")
  Position findPositionByEmployeeId(@Param("employeeId") Integer employeeId);

  Position findPositionByPositionId(Integer positionId);
}
