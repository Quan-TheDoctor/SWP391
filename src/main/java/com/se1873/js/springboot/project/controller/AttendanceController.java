package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTOList;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

  private final AttendanceService attendanceService;
  private final EmployeeService employeeService;
  private AttendanceDTOList attendanceDTOList = new AttendanceDTOList();
  @RequestMapping
  public String attendance(Model model,
                           @RequestParam(value = "startDate", required = false) LocalDate startDate,
                           @RequestParam(value = "endDate", required = false) LocalDate endDate,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "5") Integer size) {
    if(startDate == null || endDate == null) {
      startDate = LocalDate.now();
      endDate = LocalDate.now();
    }

    var attendances = attendanceService.getAll(startDate, endDate, PageRequest.of(page, size));

    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("attendances", attendances);
    return "attendance";
  }

  @RequestMapping("/create/form")
  public String createForm(Model model,
                           @RequestParam(value = "employeeIds", required = false) Integer[] employeeIds,
                           @RequestParam(value = "dates", required = false) String[] dates,
                           @RequestParam(value = "checkIns", required = false) String[] checkIns,
                           @RequestParam(value = "checkOuts", required = false) String[] checkOuts,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "5") Integer size) {
    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();

    if(employeeIds != null && employeeIds.length > 0) {
      for(int i = 0 ; i < employeeIds.length ; i++) {
        AttendanceDTO employeeDTO = attendanceService.getAttendanceByEmployeeIdAndDate(employeeIds[i], LocalDate.parse(dates[i]));
        attendanceDTOS.add(employeeDTO);
      }
      attendanceDTOList.setAttendances(attendanceDTOS);
    }
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("attendanceDTOList", attendanceDTOList);
    return "attendance-view";
  }

  @RequestMapping("/create/save")
  public String saveAttendance(Model model,
                               @ModelAttribute("attendanceDTOList") AttendanceDTOList attendanceDTOList) {
    log.info(attendanceDTOList.toString());
    attendanceService.saveAttendance(attendanceDTOList);
    return "redirect:/attendance";
  }

  @RequestMapping("/search")
  public String searchAttendance(Model model){
    return null;
  }

  @RequestMapping("/summary")
  public String summaryAttendance(Model model) {
    return "attendance-summary";
  }
}
