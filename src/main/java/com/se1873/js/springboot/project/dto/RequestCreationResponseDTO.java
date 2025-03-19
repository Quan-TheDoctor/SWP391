package com.se1873.js.springboot.project.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreationResponseDTO {
    private Integer requestId;
    private String fullName;
    private String employeeCode;
    private String positionName;
    private Double oldBaseSalary;
    private Double newBaseSalary;
    @JsonFormat(pattern = "EEEE yyyy-MM-dd")
    private LocalDate startedAt;
}
