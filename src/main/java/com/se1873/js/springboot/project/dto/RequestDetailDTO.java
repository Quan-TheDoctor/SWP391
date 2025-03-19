package com.se1873.js.springboot.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RequestDetailDTO implements Serializable {
    private Long requestId;
    private String fullName;
    private String employeeCode;
    private String positionName;
    private Double newsSalary;
    @JsonFormat(pattern = "EEEE yyyy-MM-dd")
    private LocalDate createdDate;

}
