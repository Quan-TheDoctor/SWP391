package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.AttendanceCountDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceAPI {

  private final AttendanceService attendanceService;

  @RequestMapping("/attendancesByEmployeeId")
  public ResponseEntity<AttendanceDTO> getAttendancesByEmployeeId(@RequestParam("employeeId") Integer employeeId) {
    AttendanceDTO attendances = attendanceService.getAttendanceByEmployeeIdAndDate(employeeId, LocalDate.now());

    log.info(attendances.toString());
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
}
