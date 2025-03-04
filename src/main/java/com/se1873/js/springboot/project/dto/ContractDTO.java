package com.se1873.js.springboot.project.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {
  private Integer contractId;
  private String contractType;
  private String contractCode;
  private LocalDate startDate;
  private LocalDate endDate;
  private Double baseSalary;
  private LocalDate signDate;
  private boolean isPresent;
  private LocalDateTime createdAt;
}
