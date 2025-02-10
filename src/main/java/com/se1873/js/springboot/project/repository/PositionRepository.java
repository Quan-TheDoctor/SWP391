package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
  Position findByPositionCode(String positionCode);
}
