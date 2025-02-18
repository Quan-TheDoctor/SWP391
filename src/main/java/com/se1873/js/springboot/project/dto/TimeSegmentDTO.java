package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSegmentDTO {
  private double startPercent;
  private double widthPercent;
  private String type;

  public TimeSegmentDTO(int startMinutes, int endMinutes, String type) {
    double totalMinutes = 13 * 60;
    this.startPercent = (startMinutes - 420) / totalMinutes * 100;
    this.widthPercent = (endMinutes - startMinutes) / totalMinutes * 100;
    this.type = type;
  }
}
