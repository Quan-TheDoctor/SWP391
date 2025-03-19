package com.se1873.js.springboot.project.service.position;

import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.PositionRepository;
import com.se1873.js.springboot.project.service.position.query.PositionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PositionService {
  private final PositionRepository positionRepository;
  private final PositionQueryService positionQueryService;

  @Cacheable(value = "positions", key = "'allPositions'")
  public List<Position> getAllPositions() {
    log.info("Loading positions from DB...");
    return positionRepository.findAll();
  }

  @CacheEvict(value = "positions", allEntries = true)
  public void refreshPositionsCache() {
    log.info("Clearing positions cache...");
  }

  public Position findPositionByPositionId(Integer positionId) {
    return positionQueryService.findPositionByPositionId(positionId);
  }

  public List<Position> getPositionsByDepartmentId(Integer departmentId) {
    return positionRepository.getPositionsByDepartment_DepartmentId(departmentId);
  }
}
