package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
  Position findPositionByPositionId(Integer positionId);

  List<Position> getPositionsByDepartment_DepartmentId(Integer departmentDepartmentId);
}
