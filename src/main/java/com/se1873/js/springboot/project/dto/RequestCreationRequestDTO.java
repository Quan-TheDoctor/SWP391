package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreationRequestDTO {
    private Integer departmentId;
    private String requestType;
    private Map<String,Double> employeeNamewithSalary;
}
