package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceCreationDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.DepartmentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

  private final DepartmentService departmentService;
  private final AttendanceService attendanceService;
  private Page<AttendanceDTO> attendances;
  private List<Department> departments;
  AttendanceCreationDTO attendanceCreationDTO = new AttendanceCreationDTO();

  @PostConstruct
  public void init() {
    departments = departmentService.getAllDepartments();
    attendances = attendanceService.getEmployeesAndAttendances(LocalDate.now(), LocalDate.now(), PageRequest.of(0, 10));
  }

  @RequestMapping
  public String index(Model model,
                      @RequestParam(value = "employeeIds", required = false) Integer[] employeeIds,
                      @RequestParam(value = "dates", required = false) String[] dates,
                      @RequestParam(value = "checkIns", required = false) String[] checkIns,
                      @RequestParam(value = "checkOuts", required = false) String[] checkOuts,
                      @RequestParam(value = "page", defaultValue = "0") Integer page,
                      @RequestParam(value = "size", defaultValue = "10") Integer size) {

    if(dates == null) {
      attendances = attendanceService.getEmployeesAndAttendances(LocalDate.now(), LocalDate.now(), PageRequest.of(page, size));
    } else {
      LocalDate startDate = LocalDate.parse(dates[0]);
      LocalDate endDate = LocalDate.parse(dates[1]);

      attendances = attendanceService.getEmployeesAndAttendances(startDate, endDate, PageRequest.of(page, size));
    }

    model.addAttribute("employeeIds", employeeIds);
    model.addAttribute("dates", dates);
    model.addAttribute("checkIns", checkIns);
    model.addAttribute("checkOuts", checkOuts);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("attendances", attendances);
    model.addAttribute("fragments", "fragments/attendance");
    return "index";
  }

  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam(value = "employeeIds", required = false) Integer[] employeeIds,
                       @RequestParam(value = "dates", required = false) String[] dates,
                       @RequestParam(value = "checkIns", required = false) String[] checkIns,
                       @RequestParam(value = "checkOuts", required = false) String[] checkOuts,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {

    if(dates == null) {
      attendances = attendanceService.getEmployeesAndAttendances(LocalDate.now(), LocalDate.now(), PageRequest.of(page, size));
    } else {
      log.debug(page.toString());
      log.debug(size.toString());
      LocalDate startDate = LocalDate.parse(dates[0].trim());
      LocalDate endDate = LocalDate.parse(dates[1].trim());
      log.debug(startDate.toString());
      log.debug(endDate.toString());
      attendances = attendanceService.getEmployeesAndAttendances(startDate, endDate, PageRequest.of(page, size));
      log.info(attendances.toString());
    }
    model.addAttribute("employeeIds", employeeIds);
    model.addAttribute("dates", dates);
    model.addAttribute("checkIns", checkIns);
    model.addAttribute("checkOuts", checkOuts);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("attendances", attendances);
    model.addAttribute("fragments", "fragments/attendance");
    return "index";
  }

  @RequestMapping("/create/form")
  public String createForm(Model model,
                           @RequestParam(value = "employeeIds", required = false) Integer[] employeeIds,
                           @RequestParam(value = "dates", required = false) String[] dates,
                           @RequestParam(value = "checkIns", required = false) String[] checkIns,
                           @RequestParam(value = "checkOuts", required = false) String[] checkOuts,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();
    if(employeeIds != null) {
      for(int i = 0; i < employeeIds.length; i++) {
        Integer employeeId = employeeIds[i];
        LocalDate date = LocalDate.parse(dates[i].trim());
        LocalTime checkIn = LocalTime.parse(checkIns[i].trim());
        LocalTime checkOut = LocalTime.parse(checkOuts[i].trim());

        AttendanceDTO attendance = attendanceService.getTodayAttendanceByEmployeeId(employeeId, date, checkIn, checkOut);
        attendanceDTOS.add(attendance);
      }
      attendanceCreationDTO.setAttendances(attendanceDTOS);
    }

    model.addAttribute("attendanceCreationDTO", attendanceCreationDTO);
    model.addAttribute("dates", dates);
    model.addAttribute("checkIns", checkIns);
    model.addAttribute("checkOuts", checkOuts);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("fragments", "fragments/attendance-create");
    return "index";
  }

  @RequestMapping("/create/save")
  public String save(Model model,
                     @ModelAttribute("attendanceCreationDTO") AttendanceCreationDTO attendanceCreationDTO,
                     BindingResult bindingResult) {
    if(bindingResult.hasErrors()) {
      return "redirect:/attendance/create/form";
    }
    attendanceService.updateOrCreateAttendances(attendanceCreationDTO);
    return "redirect:/attendance";
  }
}
