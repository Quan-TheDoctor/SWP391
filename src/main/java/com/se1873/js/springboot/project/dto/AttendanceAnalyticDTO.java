package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AttendanceAnalyticDTO {
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WeeklyTrendsDTO {
    private List<String> categories;
    private List<SeriesDTO> series;
    private SummaryDTO summary;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class MonthlyTrendsDTO {
    private List<String> categories;
    private List<SeriesDTO> series;
    private SummaryDTO summary;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DepartmentComparisonDTO {
    private List<DepartmentDataDTO> departments;
    private DepartmentSummaryDTO summary;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SeriesDTO {
    private String name;
    private List<Double> data;
    private String color;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DepartmentDataDTO {
    private String name;
    private Double value;
    private String color;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SummaryDTO {
    private Double weeklyAverage;
    private BestDayDTO bestDay;
    private Double improvement;

    private Double monthlyAverage;
    private BestMonthDTO bestMonth;
    private Double yearOverYearChange;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class BestDayDTO {
    private String name;
    private Double rate;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class BestMonthDTO {
    private String name;
    private Double rate;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DepartmentSummaryDTO {
    private DepartmentInfoDTO topDepartment;
    private ImprovedDepartmentDTO mostImproved;
    private DepartmentInfoDTO needsAttention;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DepartmentInfoDTO {
    private String name;
    private Double rate;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ImprovedDepartmentDTO {
    private String name;
    private Double improvement;
  }
}
