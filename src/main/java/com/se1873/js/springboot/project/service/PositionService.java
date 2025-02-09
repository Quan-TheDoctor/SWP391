package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {
  private final PositionRepository positionRepository;

  public List<Position> getAllPositions() {
    return positionRepository.findAll(Sort.by(Sort.Direction.ASC, "positionId"));
  }
}
