package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {
  private Map<Integer, Double> netSalary;
  private Map<Integer, Double> deductions;
}
