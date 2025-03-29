package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.AttendanceDTOMapper;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.*;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.position.PositionService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/attendance")
@SessionAttributes({"formattedDate", "payrollCalculationForm", "user"})
public class AttendanceController {
  private final AttendanceService attendanceService;
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final PositionService positionService;
  private AttendanceDTOList attendanceDTOList = new AttendanceDTOList();

  @ModelAttribute("payrollCalculationForm")
  public PayrollCalculationForm payrollCalculationForm() {
    return new PayrollCalculationForm();
  }

  Page<AttendanceDTO> attendances;


  @GetMapping("/search")
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String search(@RequestParam("query") String query,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       RedirectAttributes redirectAttributes,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    StringBuilder redirectUrl = new StringBuilder("/attendance?");
    redirectUrl.append("query=").append(query).append("&");
    redirectUrl.append("page=").append(page).append("&");
    redirectUrl.append("size=").append(size).append("&");
    if (startDate != null) {
      redirectUrl.append("startDate=").append(startDate).append("&");
    } else {
      redirectUrl.append("startDate=").append(LocalDate.now()).append("&");
    }

    if (endDate != null) {
      redirectUrl.append("endDate=").append(endDate).append("&");
    } else {
      redirectUrl.append("endDate=").append(LocalDate.now());
    }

    return "redirect:" + redirectUrl.toString();
  }

  @GetMapping("/filter")
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String filterStatus(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    StringBuilder redirectUrl = new StringBuilder("/attendance?");

    if (status != null && !status.isEmpty()) {
      redirectUrl.append("status=").append(status).append("&");
    }

    if (startDate != null) {
      redirectUrl.append("startDate=").append(startDate).append("&");
    } else {
      redirectUrl.append("startDate=").append(LocalDate.now()).append("&");
    }

    if (endDate != null) {
      redirectUrl.append("endDate=").append(endDate).append("&");
    } else {
      redirectUrl.append("endDate=").append(LocalDate.now()).append("&");
    }

    redirectUrl.append("page=").append(page).append("&");
    redirectUrl.append("size=").append(size);

    return "redirect:" + redirectUrl.toString();
  }

  @GetMapping("/sort")
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String sort(@RequestParam(value = "field", required = true) String field,
                     @RequestParam(value = "direction", defaultValue = "asc") String direction,
                     @RequestParam(value = "page", defaultValue = "0") Integer page,
                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                     @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                     @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                     @RequestParam(value = "status", required = false) String status,
                     @RequestParam(value = "query", required = false) String query) {

    StringBuilder redirectUrl = new StringBuilder("/attendance?");

    redirectUrl.append("sortField=").append(field).append("&");
    redirectUrl.append("direction=").append(direction).append("&");

    if (query != null && !query.isEmpty()) {
      redirectUrl.append("query=").append(query).append("&");
    }

    if (status != null && !status.isEmpty()) {
      redirectUrl.append("status=").append(status).append("&");
    }

    if (startDate != null) {
      redirectUrl.append("startDate=").append(startDate).append("&");
    }

    if (endDate != null) {
      redirectUrl.append("endDate=").append(endDate).append("&");
    }

    redirectUrl.append("page=").append(page).append("&");
    redirectUrl.append("size=").append(size);

    return "redirect:" + redirectUrl.toString();
  }



  @GetMapping
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String index(Model model,
                      @RequestParam(value = "query", required = false) String query,
                      @RequestParam(value = "view", required = false) String view,
                      @RequestParam(value = "status", required = false) String status,
                      @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                      @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                      @RequestParam(value = "sortField", required = false) String sortField,
                      @RequestParam(value = "direction", required = false) String direction,
                      @RequestParam(value = "page", defaultValue = "0") Integer page,
                      @RequestParam(value = "size", defaultValue = "10") Integer size) {

    if (startDate == null) {
      startDate = LocalDate.now();
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    Pageable pageable = PageRequest.of(page, size);

    if (query != null && !query.isEmpty()) {
      attendances = attendanceService.searchAttendanceByEmployeeName(startDate, endDate, query, pageable);
    } else if (status != null && !status.isEmpty() && !status.equals("ALL")) {
      attendances = attendanceService.filterByStatus(startDate, endDate, pageable, status);
    } else if(sortField != null) {
      List<AttendanceDTO> sortedList = attendances.stream()
        .sorted(getComparator(sortField, direction))
        .toList();

      attendances = new PageImpl<>(sortedList, pageable, sortedList.size());
    } else {
      attendances = attendanceService.getAll(startDate, endDate, pageable);
    }

    model.addAttribute("query", query);
    model.addAttribute("view", view);
    model.addAttribute("status", status);
    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("sortField", sortField != null ? sortField : "");
    model.addAttribute("direction", direction != null ? direction : "");
    model.addAttribute("attendances", attendances);
    model.addAttribute("contentFragment", "fragments/attendance-fragments");
    model.addAttribute("baseUrl", "/attendance");

    return "index";
  }


  private java.util.Comparator<AttendanceDTO> getComparator(String field, String direction) {
    return switch (field) {
      case "employeeFirstName" ->
        Comparator.comparing(AttendanceDTO::getEmployeeFirstName, direction.equals("desc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "attendanceDate" ->
        Comparator.comparing(AttendanceDTO::getAttendanceDate, direction.equals("desc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "attendanceCheckIn" ->
        Comparator.comparing(AttendanceDTO::getAttendanceCheckIn, direction.equals("desc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "attendanceCheckOut" ->
        Comparator.comparing(AttendanceDTO::getAttendanceCheckOut, direction.equals("desc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "attendanceStatus" ->
        Comparator.comparing(AttendanceDTO::getAttendanceStatus, direction.equals("desc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "attendanceOvertimeHours" ->
        Comparator.comparing(AttendanceDTO::getAttendanceOvertimeHours, direction.equals("desc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      default -> Comparator.comparing(AttendanceDTO::getEmployeeId, Comparator.naturalOrder());
    };
  }

  @RequestMapping("/create/form")
  @PreAuthorize("hasPermission('ATTENDANCE', 'ADD')")
  public String createForm(Model model,
                           @RequestParam(value = "employeeIds", required = false) Integer[] employeeIds,
                           @RequestParam(value = "dates", required = false) String[] dates,
                           @RequestParam(value = "checkIns", required = false) String[] checkIns,
                           @RequestParam(value = "checkOuts", required = false) String[] checkOuts,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "5") Integer size) {
    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();

    if (employeeIds != null && employeeIds.length > 0) {
      for (int i = 0; i < employeeIds.length; i++) {
        AttendanceDTO employeeDTO = attendanceService.getAttendanceByEmployeeIdAndDate(employeeIds[i], LocalDate.parse(dates[i]));
        attendanceDTOS.add(employeeDTO);
      }
      attendanceDTOList.setAttendances(attendanceDTOS);
    }
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("attendanceDTOList", attendanceDTOList);
    model.addAttribute("contentFragment", "fragments/attendance-create-fragments");
    return "index";
  }

  @PostMapping("/create/save")
  @PreAuthorize("hasPermission('ATTENDANCE', 'ADD')")
  public String saveAttendance(@Valid @ModelAttribute("attendanceDTOList") AttendanceDTOList attendanceDTOList,
                               BindingResult result) {
    if (result.hasErrors()) {
      log.error("Validation errors: {}", result.getAllErrors());
      return "redirect:/attendance/create/form?error";
    }
    attendanceService.saveAttendance(attendanceDTOList);
    return "redirect:/attendance";
  }

  @RequestMapping("/export")
  @PreAuthorize("hasPermission('ATTENDANCE', 'UPDATE')")
  public ResponseEntity<Resource> exportAttendance(
          @RequestParam(value = "selectedEmployees", required = false) String selectedEmployees,
          @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
          @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    List<Integer> employeeIds = (selectedEmployees != null && !selectedEmployees.isEmpty())
            ? Arrays.stream(selectedEmployees.split(",")).map(Integer::parseInt).collect(Collectors.toList())
            : null;

    if (startDate == null) {
      startDate = LocalDate.of(2020, 1, 1);
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    Resource file = attendanceService.exportAttendanceToExcel(employeeIds, startDate, endDate);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
  }

  @RequestMapping("/export/view")
  @PreAuthorize("hasPermission('ATTENDANCE', 'UPDATE')")
  public String viewExportAttendance(
          @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
          @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
          @RequestParam(value = "status", required = false) String status,
          @RequestParam(value = "page", defaultValue = "0") Integer page,
          @RequestParam(value = "size", defaultValue = "10") Integer size,
          Model model) {

    if (startDate == null || endDate == null) {
      startDate = LocalDate.of(2020, 1, 1);
      endDate = LocalDate.now();
    }

    Page<AttendanceDTO> attendances = attendanceService.getAll(startDate, endDate, status, PageRequest.of(page, size));

    model.addAttribute("attendances", attendances);
    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    model.addAttribute("status", status);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("totalAttendances", attendances.getTotalElements());
    model.addAttribute("totalPages", attendances.getTotalPages());
    model.addAttribute("currentPage", page + 1);
    model.addAttribute("contentFragment", "fragments/attendance-export-fragments");

    return "index";
  }
  @RequestMapping("/status")
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String status(@RequestParam(value = "month", required = false) String month,
                       @RequestParam(value = "year", required = false) String year,
                       @RequestParam(value = "action", required = false) String action,
                       @RequestParam(value = "query", required = false) String query,
                       Model model, Pageable pageable) {

    LocalDate currentDate = LocalDate.now();
    String currentMonth = String.format("%02d", currentDate.getMonthValue());
    String currentYear = String.valueOf(currentDate.getYear());

    String formattedDate = (String) model.getAttribute("formattedDate");

    if (formattedDate == null || month != null || year != null) {
      if (month == null) month = currentMonth;
      if (year == null) year = currentYear;
      formattedDate = year + "-" + month;
      model.addAttribute("formattedDate", formattedDate);
    }

    String[] parts = formattedDate.split("-");
    String selectedYear = parts[0];
    String selectedMonth = parts[1];

    model.addAttribute("selectedMonth", selectedMonth);
    model.addAttribute("selectedYear", selectedYear);

    if (action == null) {
      action = (String) model.getAttribute("action");
    }
    if (query == null) {
      query = (String) model.getAttribute("query");
    }

    System.out.println("Action: " + action);
    System.out.println("Query: " + query);

    List<Position> positions = positionService.getAllPositions();
    List<Department> departments = departmentService.getAllDepartments();
    List<EmployeeAttendanceStatusDTO> employeeAttendanceStatusDTOS = new ArrayList<>();

    if ("search".equals(action) && query != null && !query.trim().isEmpty()) {
      employeeAttendanceStatusDTOS = attendanceService.findEmployeeAttendanceStatusbyEmployeeName(query, pageable, formattedDate);
    } else if ("departmentfilter".equals(action) && query != null && !query.trim().isEmpty()) {
      System.out.println("checllllllll");
      if ("all".equals(query)) {
        employeeAttendanceStatusDTOS = attendanceService.getEmployeeAttendanceStatus(formattedDate, pageable);
      } else {
        employeeAttendanceStatusDTOS = attendanceService.departmentFilter(query, pageable, formattedDate);
      }
    } else if ("positionfilter".equals(action) && query != null && !query.trim().isEmpty()) {
      if ("all".equals(query)) {
        employeeAttendanceStatusDTOS = attendanceService.getEmployeeAttendanceStatus(formattedDate, pageable);
      } else {

        employeeAttendanceStatusDTOS = attendanceService.positionfilter(query, pageable, formattedDate);
      }
    }else {
      employeeAttendanceStatusDTOS = attendanceService.getEmployeeAttendanceStatus(formattedDate, pageable);
    }

    Map<String, EmployeeAttendanceStatusDTO> mostLateAndAbsent = attendanceService.findEmployeesWithMostLateAndAbsent(employeeAttendanceStatusDTOS);
    EmployeeAttendanceStatusDTO mostLate = mostLateAndAbsent.get("MostLate");
    EmployeeAttendanceStatusDTO mostAbsent = mostLateAndAbsent.get("MostAbsent");

    int employeeCount = employeeService.countEmployees();
    model.addAttribute("employeeCount", employeeCount);
    model.addAttribute("mostLate", mostLate);
    model.addAttribute("mostAbsent", mostAbsent);
    model.addAttribute("employeeAttendanceStatusDTOS", employeeAttendanceStatusDTOS);
    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);

    return "fragments/employee-attendance-status";
  }

  @GetMapping("/searchstatus")
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String searchstatus(@RequestParam("query") String search,
                              RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("action", "search");
    redirectAttributes.addFlashAttribute("query", search);
    return "redirect:/attendance/status";

  }
  @GetMapping("/filterEmployee")
  @PreAuthorize("hasPermission('ATTENDANCE', 'VISIBLE')")
  public String filterEmployee(@RequestParam(value = "department", required = false) String department,
                                 @RequestParam(value = "position", required = false) String position,
                                 RedirectAttributes redirectAttributes) {

    if (department != null && !department.isEmpty()&& !department.equals("all") ) {
      System.out.println(department);
      redirectAttributes.addFlashAttribute("action", "departmentfilter");
      redirectAttributes.addFlashAttribute("query", department);
    }

    if (position != null && !position.isEmpty() && !position.equals("all")) {
      System.out.println(position);
      redirectAttributes.addFlashAttribute("action", "positionfilter");
      redirectAttributes.addFlashAttribute("query", position);
    }

    return "redirect:/attendance/status";
  }
}
