package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.PositionRepository;
import com.se1873.js.springboot.project.service.position.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PositionAPI {


  private final PositionService positionService;
  private final PositionRepository positionRepository;

  @GetMapping("/positions")
  public ResponseEntity<List<Position>> getPositionsByDepartment(
    @RequestParam Integer departmentId
  ) {
    List<Position> positions = positionService.getPositionsByDepartmentId(departmentId);
    return ResponseEntity.ok(positions);
  }

  @GetMapping("/positionsByName")
  public ResponseEntity<List<Position>> getPositionsByDepartmentName(
    @RequestParam String departmentName
  ) {
    List<Position> positions = positionRepository.getPositionsByDepartment_DepartmentName(departmentName);
    return ResponseEntity.ok(positions);
  }
}
