package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.entity.Position;
import com.se1873.js.springboot.management.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {
  @Autowired
  private PositionRepository positionRepository;

  public List<Position> findAll() {
    return positionRepository.findAll();
  }
}
