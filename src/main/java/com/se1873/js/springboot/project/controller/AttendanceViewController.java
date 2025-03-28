package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.mapper.AttendanceDTOMapper;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance/view")
public class AttendanceViewController {

  private final AttendanceDTOMapper attendanceDTOMapper;
  private final AttendanceRepository attendanceRepository;

  public AttendanceViewController(AttendanceDTOMapper attendanceDTOMapper, AttendanceRepository attendanceRepository) {
    this.attendanceDTOMapper = attendanceDTOMapper;
    this.attendanceRepository = attendanceRepository;
  }

  @GetMapping
  public String viewAttendance(@RequestParam(value = "attendanceId", required = false) Integer attendanceId, Model model, RedirectAttributes redirectAttributes) {
    if(attendanceId == null) {
      redirectAttributes.addFlashAttribute("message", "Employee haven't check attendance yet, no details");
      redirectAttributes.addFlashAttribute("messageType", "error");
      return "redirect:/attendance";
    }
    AttendanceDTO attendance = attendanceDTOMapper.toDTO(attendanceRepository.getAttendanceByAttendanceId(attendanceId));

    calculateWorkHours(attendance);

    LocalDate attendanceDate = attendance.getAttendanceDate();
    LocalDate startOfMonth = attendanceDate.withDayOfMonth(1);
    LocalDate endOfMonth = attendanceDate.withDayOfMonth(attendanceDate.lengthOfMonth());

    List<AttendanceDTO> monthlyAttendance = attendanceRepository.getAttendanceByDateBetweenAndEmployee_EmployeeId(
      startOfMonth,
      endOfMonth,
      attendance.getEmployeeId()
    ).stream().map(attendanceDTOMapper::toDTO).collect(Collectors.toList());

    monthlyAttendance.forEach(this::calculateWorkHours);

    int onTimeDays = (int) monthlyAttendance.stream()
      .filter(a -> a.getAttendanceStatus().equalsIgnoreCase("On time"))
      .count();

    int lateDays = (int) monthlyAttendance.stream()
      .filter(a -> a.getAttendanceStatus().equalsIgnoreCase("Late"))
      .count();

    int absentDays = (int) monthlyAttendance.stream()
      .filter(a -> a.getAttendanceStatus().equalsIgnoreCase("Absent"))
      .count();

    double totalOTHours = monthlyAttendance.stream()
      .mapToDouble(AttendanceDTO::getAttendanceOvertimeHours)
      .sum();

    model.addAttribute("attendance", attendance);
    model.addAttribute("monthlyAttendance", monthlyAttendance);
    model.addAttribute("monthlyStats", Map.of(
      "onTimeDays", onTimeDays,
      "lateDays", lateDays,
      "absentDays", absentDays,
      "totalOTHours", totalOTHours
    ));
    model.addAttribute("contentFragment", "fragments/attendance-view-fragments");
    return "index";
  }

  @PostMapping("/update")
  public String updateAttendance(@RequestParam("attendanceId") Integer attendanceId,
                                 @RequestParam("attendanceCheckIn") String checkIn,
                                 @RequestParam("attendanceCheckOut") String checkOut,
                                 @RequestParam("attendanceStatus") String status,
                                 @RequestParam("attendanceOvertimeHours") Double overtimeHours,
                                 @RequestParam(value = "attendanceNotes", required = false) String notes) {

    Attendance attendance = attendanceRepository.getAttendanceByAttendanceId(attendanceId);

    if (attendance != null) {
      if (checkIn != null && !checkIn.isEmpty()) {
        attendance.setCheckIn(LocalTime.parse(checkIn));
      }

      if (checkOut != null && !checkOut.isEmpty()) {
        attendance.setCheckOut(LocalTime.parse(checkOut));
      }

      attendance.setStatus(status);
      attendance.setOvertimeHours(overtimeHours);

      if (notes != null) {
//        attendance.setNotes(notes);
      }

      attendanceRepository.save(attendance);
    }

    return "redirect:/attendance/view?attendanceId=" + attendanceId;
  }

  @GetMapping("/delete")
  public String deleteAttendance(@RequestParam("attendanceId") Integer attendanceId) {
    Attendance attendance = attendanceRepository.getAttendanceByAttendanceId(attendanceId);

    if (attendance != null) {
      attendanceRepository.save(attendance);
    }

    return "redirect:/attendance";
  }


//  @GetMapping("/view/export/pdf")
//  public ResponseEntity<Resource> exportAttendanceToPdf(@RequestParam("attendanceId") Integer attendanceId) {
//    AttendanceDTO attendance = attendanceDTOMapper.toDTO(attendanceRepository.getAttendanceByAttendanceId(attendanceId));
//
//    Resource pdfResource = attendanceService.generateAttendancePdf(attendance);
//
//    return ResponseEntity.ok()
//      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + attendanceId + ".pdf")
//      .contentType(MediaType.APPLICATION_PDF)
//      .body(pdfResource);
//  }
//
//  @GetMapping("/view/export/excel")
//  public ResponseEntity<Resource> exportAttendanceToExcel(@RequestParam("attendanceId") Integer attendanceId) {
//    AttendanceDTO attendance = attendanceDTOMapper.toDTO(attendanceRepository.getAttendanceByAttendanceId(attendanceId));
//
//    Resource excelResource = attendanceService.generateAttendanceExcel(attendance);
//
//    return ResponseEntity.ok()
//      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + attendanceId + ".xlsx")
//      .contentType(MediaType.APPLICATION_OCTET_STREAM)
//      .body(excelResource);
//  }
//
//  @GetMapping("/view/print")
//  public String printAttendance(@RequestParam("attendanceId") Integer attendanceId, Model model) {
//    AttendanceDTO attendance = attendanceDTOMapper.toDTO(attendanceRepository.getAttendanceByAttendanceId(attendanceId));
//
//    calculateWorkHours(attendance);
//
//    model.addAttribute("attendance", attendance);
//    model.addAttribute("printMode", true);
//
//    return "fragments/attendance-print";
//  }



  private void calculateWorkHours(AttendanceDTO attendance) {
    if (attendance == null ||
      attendance.getAttendanceCheckIn() == null ||
      attendance.getAttendanceCheckOut() == null) {

      attendance.setAttendanceWorkHours(0.0);
      return;
    }

    try {
      LocalTime checkIn = attendance.getAttendanceCheckIn();
      LocalTime checkOut = attendance.getAttendanceCheckOut();

      int checkInMinutes = checkIn.getHour() * 60 + checkIn.getMinute();
      int checkOutMinutes = checkOut.getHour() * 60 + checkOut.getMinute();

      int diffMinutes = checkOutMinutes - checkInMinutes;

      if (diffMinutes < 0) {
        diffMinutes += 24 * 60;
      }

      diffMinutes -= 60;

      if (diffMinutes < 0) {
        attendance.setAttendanceWorkHours(0.0);
        return;
      }

      double workHours = Math.round((diffMinutes / 60.0) * 10) / 10.0;
      attendance.setAttendanceWorkHours(workHours);

    } catch (Exception e) {
      attendance.setAttendanceWorkHours(0.0);
    }
  }

}
