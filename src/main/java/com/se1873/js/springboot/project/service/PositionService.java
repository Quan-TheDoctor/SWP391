package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PositionService {

  private final PositionRepository positionRepository;

  public List<Position> getPositionsByDepartmentId(Integer departmentId) {
    return positionRepository.getPositionsByDepartment_DepartmentId(departmentId);
  }
}
