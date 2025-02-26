package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
  private Integer requestId;
  private Integer requesterId;
  private String requesterName;
  private String reason;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate requestDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  private Integer totalDays;

  private String requestStatus;
  private String note;
  private String approvalName;
  private String requestType;
}
