package com.se1873.js.springboot.project.service.position.query;

import com.se1873.js.springboot.project.entity.Position;

public interface PositionQueryService {
  Position findPositionByPositionId(Integer positionId);
}
