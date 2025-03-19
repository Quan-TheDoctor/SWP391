package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceCountDTO {
    Integer totalEmployee;
    Integer workedEmployee;
    Integer lateEmployee;
    Integer absenceEmployee;

}
