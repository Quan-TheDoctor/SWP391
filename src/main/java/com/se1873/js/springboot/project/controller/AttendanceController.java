package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.DepartmentService;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final DepartmentService departmentService;
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

}
