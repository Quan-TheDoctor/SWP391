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
    private String requestType;
    private String requestContent;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestDate;
    private String requestStatus;

    private String approvalName;
}
