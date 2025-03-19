package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreationRequestDTO {
    private Integer departmentId;
    private String requestType;
    private String employeeName;
    private Double salaryIncreasePercentage;
}
