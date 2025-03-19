package com.se1873.js.springboot.project.dto;


import com.se1873.js.springboot.project.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAttendanceStatusDTO {
    private EmployeeDTO employee;
    private int countLateDays;
    private int countAbsentDays;
    @DateTimeFormat(pattern = "yyyy-MM")
    private YearMonth monthYear;
}
