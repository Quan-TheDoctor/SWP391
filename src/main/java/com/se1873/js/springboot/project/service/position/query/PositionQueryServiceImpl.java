package com.se1873.js.springboot.project.service.position.query;

import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.PositionRepository;
import org.springframework.stereotype.Service;

@Service
public class PositionQueryServiceImpl implements PositionQueryService {
  private final PositionRepository positionRepository;

  public PositionQueryServiceImpl(PositionRepository positionRepository) {
    this.positionRepository = positionRepository;
  }

  @Override
  public Position findPositionByPositionId(Integer positionId) {
    return positionRepository.findPositionByPositionId(positionId);
  }
}
