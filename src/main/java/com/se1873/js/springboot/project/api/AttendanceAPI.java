package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceAPI {

  private final AttendanceService attendanceService;
  private final EmployeeService employeeService;

  @RequestMapping("/attendancesByEmployeeId")
  public ResponseEntity<AttendanceDTO> getAttendancesByEmployeeId(@RequestParam("employeeId") Integer employeeId) {
    AttendanceDTO attendances = attendanceService.getAttendanceByEmployeeIdAndDate(employeeId, LocalDate.now());
    return ResponseEntity.ok(attendances);
  }

  @RequestMapping("/getAllAttendanceByDateAndEmployeeId")
  public ResponseEntity<List<AttendanceDTO>> getAllAttendanceByDateAndEmployeeId(
    @RequestParam("employeeId") Integer employeeId,
    @RequestParam("date") String date
  ) {
    LocalDate localDate = LocalDate.parse(date);
    var attendances = attendanceService.getAttendancesByEmployeeIdAndDate(employeeId, localDate);
    return ResponseEntity.ok(attendances);
  }

  @RequestMapping("/attendancesByDay")
  public ResponseEntity<AttendanceCountDTO> getAttendancesByDay(
    @RequestParam("date") LocalDate date) {

    AttendanceCountDTO attendanceCountDTO = attendanceService.countAvailableAttendance(String.valueOf(date));
    System.out.println("absent: " +attendanceCountDTO.getAbsentEmployee());
    return ResponseEntity.ok(attendanceCountDTO);
  }

  @GetMapping("/weekly-trends")
  public ResponseEntity<AttendanceAnalyticDTO.WeeklyTrendsDTO> getWeeklyTrends(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    AttendanceAnalyticDTO.WeeklyTrendsDTO data = attendanceService.getWeeklyTrends(year, month, startDate, endDate);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/monthly-trends")
  public ResponseEntity<AttendanceAnalyticDTO.MonthlyTrendsDTO> getMonthlyTrends(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    AttendanceAnalyticDTO.MonthlyTrendsDTO data = attendanceService.getMonthlyTrends(year, startDate, endDate);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/department-comparison")
  public ResponseEntity<AttendanceAnalyticDTO.DepartmentComparisonDTO> getDepartmentComparison(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    AttendanceAnalyticDTO.DepartmentComparisonDTO data = attendanceService.getDepartmentComparison(year, month, date);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/recognize")
  public ResponseEntity<?> processAttendance(@RequestBody AttendanceRequest request) {
    try {
      EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(request.getId());
      AttendanceDTO attendance = attendanceService.getAttendanceByEmployeeIdAndDate(employee.getEmployeeId(), LocalDate.now());
      attendance.setAttendanceCheckIn(LocalTime.now());
      attendance.setAttendanceCheckOut(LocalTime.now().plusMinutes(2));
      attendance.setAttendanceStatus("Co mat");

      AttendanceDTOList list = new AttendanceDTOList();
      list.setAttendances(List.of(attendance));

      attendanceService.saveAttendance(list);

      return ResponseEntity.ok().body(Map.of(
        "status", "success",
        "employee", request.getName()
      ));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of(
        "status", "error",
        "message", e.getMessage()
      ));
    }
  }

  @PutMapping("/update")
  public ResponseEntity<?> updateAttendanceRecord(@RequestBody AttendanceDTO record) {
    Double overtimeHours = calculateOvertimeHours(record.getAttendanceCheckIn(), record.getAttendanceCheckOut());

    if (record.getAttendanceOvertimeHours() == null || record.getAttendanceOvertimeHours() == 0) {
      record.setAttendanceOvertimeHours(overtimeHours);
    }

    attendanceService.updateAttendanceRecord(record);

    return ResponseEntity.ok().body(Map.of(
      "success", true,
      "message", "Attendance record updated successfully"
    ));
  }

  private Double calculateOvertimeHours(LocalTime checkIn, LocalTime checkOut) {
    if (checkIn == null || checkOut == null) {
      return 0.0;
    }

    try {
      LocalTime standardEnd = LocalTime.of(18, 0);

      double overtimeHours = 0.0;

      if (checkOut.isAfter(standardEnd)) {
        long overtimeMinutes = ChronoUnit.MINUTES.between(standardEnd, checkOut);
        overtimeHours = Math.round(overtimeMinutes / 6.0) / 10.0;
      }

      return overtimeHours;
    } catch (Exception e) {
      log.error("Error calculating overtime hours: {}", e.getMessage());
      return 0.0;
    }
  }



  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AttendanceRequest {
    private int id;
    private String name;
    private String timestamp;
  }
}
