package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PositionDTO {
  private Integer positionId;
  private String positionName;
  private String positionDescription;
  private String positionCode;
  private Integer positionLevel;
  private LocalDateTime positionCreatedAt;
}
