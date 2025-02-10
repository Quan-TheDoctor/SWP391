package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PositionService {


  private final PositionRepository positionRepository;

  public List<Position> getAllPositions() {
    return positionRepository.findAll(Sort.by(Sort.Direction.ASC, "positionId"));
  }
}
