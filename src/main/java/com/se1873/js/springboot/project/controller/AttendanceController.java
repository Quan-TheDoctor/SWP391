package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.DepartmentService;
import com.se1873.js.springboot.project.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {
  private final AttendanceService attendanceService;
  private final DepartmentService departmentService;
  private final EmployeeService employeeService;
  private List<Department> departments;

  @PostConstruct
  public void init() {
    departments = departmentService.getAllDepartments();
  }

  @RequestMapping
  public String attendance(Model model,
                           @RequestParam(value = "filterDate", required = false) LocalDate filterDate,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
    if (filterDate == null) {
      filterDate = LocalDate.now().withDayOfMonth(1);
    } else {
      filterDate = filterDate.withDayOfMonth(1);
    }

    int year = filterDate.getYear();
    int month = filterDate.getMonthValue();

    Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
    Page<AttendanceDTO> attendanceDTOS = attendanceService.getAttendanceByMonth(year, month, pageable);
    model.addAttribute("filterDate", filterDate);
    model.addAttribute("departments", departments);
    model.addAttribute("attendances", attendanceDTOS);
    model.addAttribute("fragments", "fragments/attendance");
    return "index";
  }


  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam(value = "field", required = false) String field,
                       @RequestParam(value = "value", required = false) String value,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
    Integer intValue = "all".equals(value) ? null : Integer.parseInt(value);
    var attendances = attendanceService.filterAttendancesByField(field, intValue, pageable);
    model.addAttribute("departments", departments);
    model.addAttribute("attendances", attendances);
    model.addAttribute("fragments", "fragments/attendance");
    return "index";
  }

  @RequestMapping("/view")
  public String viewAttendance(Model model) {

    model.addAttribute("departments", departments);
    model.addAttribute("fragments", "fragments/attendance-details");
    return "index";
  }

  @RequestMapping("/view/getEmployees")
  public String filterEmployee(Model model,
                               @RequestParam("field") String field,
                               @RequestParam(value = "value", required = false, defaultValue = "all") String value,
                               @RequestParam(value = "employeeId", required = false) Integer employeeId) {
    List<AttendanceDTO> attendanceDTO = null;
    EmployeeDTO employeeDTO = null;
    Pageable pageable = PageRequest.of(0, 100, Sort.by("employeeId").ascending());
    Integer intValue = "all".equals(value) ? null : Integer.parseInt(value);
    Page<EmployeeDTO> employees = employeeService.filter(field, intValue, pageable);
    if (employeeId != null) {
      attendanceDTO = attendanceService.findAttendancesByEmployeeId(employeeId);
      employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
    }

    model.addAttribute("AttendanceDTO", attendanceDTO);
    model.addAttribute("employeeDTO", employeeDTO);
    model.addAttribute("value", value);
    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("fragments", "fragments/attendance-details");
    return "index";
  }


  @RequestMapping("/view/detail")
  public String detail(Model model,
                       @RequestParam(value = "field", required = false, defaultValue = "null") String field,
                       @RequestParam(value = "value", required = false, defaultValue = "all") String value,
                       @RequestParam(value = "employeeId", required = false) Integer employeeId,
                       @RequestParam(value = "filterDate", required = false) LocalDate filterDate) {
    List<AttendanceDTO> attendanceDTO = null;
    EmployeeDTO employeeDTO = null;
    String dateInText = "";
    Pageable pageable = PageRequest.of(0, 100, Sort.by("employeeId").ascending());
    Integer intValue = "all".equals(value) ? null : Integer.parseInt(value);
    Page<EmployeeDTO> employees = employeeService.filter(field, intValue, pageable);
    if(filterDate == null) {
      filterDate = LocalDate.now();
      dateInText = Month.of(filterDate.getMonthValue()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + filterDate.getYear();
    } else {
      filterDate = LocalDate.now();
      dateInText = Month.of(filterDate.getMonthValue()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + filterDate.getYear();
    }

    int year = filterDate.getYear();
    int month = filterDate.getMonthValue();

    if (employeeId != null) {
      attendanceDTO = attendanceService.findAttendancesByEmployeeIdInSpecifictime(employeeId, year, month);
      employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
      double totalOverHours = attendanceDTO.stream().map(AttendanceDTO::getAttendance).mapToDouble(Attendance::getOvertimeHours).sum();
      double totalWorkingTime = attendanceDTO.stream().map(AttendanceDTO::getAttendance).mapToDouble(a -> {
        Duration duration = Duration.between(a.getCheckIn(), a.getCheckOut());

        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();

        int totalMinutes = hours * 60 + minutes;

        return (double) totalMinutes / 60.0;
      }).sum();

      String formattedOverHours = getTotalHourInString(totalOverHours);
      String formattedTime = getTotalHourInString(totalWorkingTime);
      model.addAttribute("totalOverHours", formattedOverHours);
      model.addAttribute("totalWorkingTimes", formattedTime);
    }
    model.addAttribute("dateInText", dateInText);
    model.addAttribute("selectedDate", filterDate);
    model.addAttribute("attendances", attendanceDTO);
    model.addAttribute("employee", employeeDTO);
    model.addAttribute("value", value);
    model.addAttribute("field", field);
    model.addAttribute("employees", employees);
    model.addAttribute("departments", departments);
    model.addAttribute("fragments", "fragments/attendance-details");
    return "index";
  }

  private String getTotalHourInString(double duration) {
    int totalTime = (int) Math.round(duration * 60);
    int hours = totalTime / 60;
    int minutes = totalTime % 60;

    return String.format("%02d giờ %02d phút", hours, minutes);
  }

  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("search") String search,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size){
    Pageable pageable = PageRequest.of(page, size, Sort.by("employeeId").ascending());
    Page<AttendanceDTO> attendance = attendanceService.search(search, pageable);
    model.addAttribute("attendance",attendance);
    model.addAttribute("departments", departments);

    model.addAttribute("fragments", "fragments/employee");
    return "attendance";
  }
}
