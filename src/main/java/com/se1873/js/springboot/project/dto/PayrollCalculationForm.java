package com.se1873.js.springboot.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollCalculationForm {
  private Integer requesterId;
  private Integer selectedDepartmentId;
  @Valid
  @NotEmpty(message = "Vui lòng thêm ít nhất 1 nhân viên")
  private List<PayrollCalculationDTO> payrollCalculations = new ArrayList<>();
}
