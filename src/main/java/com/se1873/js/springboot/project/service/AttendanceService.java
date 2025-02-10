package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        List<Attendance> attendances = new ArrayList<>();
        attendances = attendanceRepository.findAttendancesByDate(date);
        AttendanceDTO attendanceDTO = null;
        List<AttendanceDTO> attendanceDTOs = new ArrayList<>();
        for (Attendance attendance : attendances) {
            Employee employee = attendance.getEmployee();
            attendanceDTO = AttendanceDTO.builder()
                    .employee(employee).attendance(attendance).build();
            log.info(attendanceDTO.toString());

            attendanceDTOs.add(attendanceDTO);
        }
        return attendanceDTOs;
    }

    public List<AttendanceDTO> getAllAttendance() {
        List<Attendance> attendances = new ArrayList<>();
        attendances = attendanceRepository.findAttendances();
        AttendanceDTO attendanceDTO = null;
        List<AttendanceDTO> attendanceDTOs = new ArrayList<>();
        for (Attendance attendance : attendances) {
            Employee employee = attendance.getEmployee();
            attendanceDTO = AttendanceDTO.builder()
                    .employee(employee).attendance(attendance).build();
            log.info(attendanceDTO.toString());

            attendanceDTOs.add(attendanceDTO);
        }
        return attendanceDTOs;

    }


}
