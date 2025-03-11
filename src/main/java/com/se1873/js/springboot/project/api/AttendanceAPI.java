package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.AttendanceCountDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTOList;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.EmployeeService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @RequestParam("date") String date) {
    AttendanceCountDTO attendanceCountDTO = attendanceService.countAvailableAttendance(date);
    log.info(attendanceCountDTO.toString());
    return ResponseEntity.ok(attendanceCountDTO);
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
