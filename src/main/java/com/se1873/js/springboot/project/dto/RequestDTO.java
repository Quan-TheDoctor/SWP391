package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.SalaryRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
  private Integer requestId;
  private Integer requesterId;
  private String requesterName;
  private String reason;

  private List<Integer> payrollIds;

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
  private Integer approvalId;
  private String requestType;
  private LeaveDTO leaveDTO;
  private List<SalaryRecord> salaryRecords;
}
