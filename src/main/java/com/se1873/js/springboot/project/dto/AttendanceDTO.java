package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDTO {
    private Integer employeeId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String idNumber;
    private String permanentAddress;
    private String temporaryAddress;
    private String personalEmail;
    private String companyEmail;
    private String phoneNumber;
    private String maritalStatus;
    private String bankAccount;
    private String bankName;
    private String taxCode;

    private Integer attendanceId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime attendanceCheckIn;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime attendanceCheckOut;
    private String attendanceStatus;
    private Double attendanceOvertimeHours;
    private String attendanceNote;

    private List<TimeSegmentDTO> segments;
}
