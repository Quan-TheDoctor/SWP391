package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentHistoryDTO {
  private Integer employmentHistoryId;
  private Boolean employmentHistoryIsCurrent;
  private String transferReason;
  private String decisionNumber;
  private LocalDateTime employmentHistoryCreatedAt;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate employmentHistoryStartDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate employmentHistoryEndDate;
}
