package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDTO {
  private List<String> months;
  private List<SeriesData> series;
  private MonthlySummary summary;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeriesData {
    private String name;
    private String color;
    private List<Double> data;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MonthlySummary {
    private double monthlyAverage;
    private BestMonth bestMonth;
    private Double yearOverYearChange;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BestMonth {
    private String name;
    private double rate;
  }
}
