package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDTO {
    private Employee employee;
    private Attendance attendance;
    private List<TimeSegmentDTO> segments;
}
