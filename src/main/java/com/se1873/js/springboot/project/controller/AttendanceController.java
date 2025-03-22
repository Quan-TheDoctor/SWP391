package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.entity.User;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
  private final SalaryRecordService salaryRecordService;
  private AttendanceDTOList attendanceDTOList = new AttendanceDTOList();

  @ModelAttribute("payrollCalculationForm")
  public PayrollCalculationForm payrollCalculationForm() {
    return new PayrollCalculationForm();
  }

  Page<AttendanceDTO> attendances;


  @GetMapping("/search")
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

  @RequestMapping("/demo")
  public String attendance(Model model,
                           @RequestParam(value = "query", required = false) String query,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                           @RequestParam(value = "view", required = false) String view) {
    if("kanban".equals(view)) {
      List<AttendanceDTO> allAttendances = attendanceService.getAllAttendances();
      model.addAttribute("allAttendances", allAttendances);
    }
    if (startDate == null || endDate == null) {
      startDate = LocalDate.now();
      endDate = LocalDate.now();
    }
    attendances = (Page<AttendanceDTO>) model.asMap().get("attendances");
    Map<String,Integer> quantity = attendanceService.getQuantity();
    if(attendances == null)
      attendances = attendanceService.getAll(startDate, endDate, PageRequest.of(page, size));

    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("quantity",quantity);
    model.addAttribute("attendances", attendances);
    model.addAttribute("contentFragment", "fragments/attendance-fragments");
    return "index";
  }

//  @RequestMapping("/filter")
//  public String filterStatus(Model model,
//                             @RequestParam(value = "status", required = false) String status,
//                             @RequestParam(value = "page", defaultValue = "0") Integer page,
//                             @RequestParam(value = "size", defaultValue = "10") Integer size,
//                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
//    if (startDate == null || endDate == null) {
//      startDate = LocalDate.now();
//      endDate = LocalDate.now();
//    }
//    attendances = attendanceService.filterByStatus(startDate, endDate, PageRequest.of(page,size), status);
//
//    Map<String,Integer> quantity = attendanceService.getQuantity();
//
//    model.addAttribute("quantity",quantity);
//    model.addAttribute("attendances",attendances);
//    model.addAttribute("startDate", startDate);
//    model.addAttribute("endDate", endDate);
//    model.addAttribute("page", page);
//    model.addAttribute("size", size);
//    model.addAttribute("contentFragment", "fragments/attendance-fragments");
//    return "index";
//  }

  @RequestMapping("/create/form")
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
    model.addAttribute("contentFragment", "fragments/attendance-view-fragments");
    return "index";
  }

  @PostMapping("/create/save")
  public String saveAttendance(@Valid @ModelAttribute("attendanceDTOList") AttendanceDTOList attendanceDTOList,
                               BindingResult result) {
    if (result.hasErrors()) {
      log.error("Validation errors: {}", result.getAllErrors());
      return "redirect:/attendance/create/form?error";
    }
    attendanceService.saveAttendance(attendanceDTOList);
    return "redirect:/attendance";
  }


  @RequestMapping("/summary")
  public String showPage(@ModelAttribute PayrollCalculationForm form,
                         Model model,
                         @ModelAttribute("loggedInUser") User loggedInUser) {
    if (loggedInUser == null) {
      return "redirect:/login";
    }

    if (form.getSelectedDepartmentId() != null) {
      form.getPayrollCalculations().clear();
      Page<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(form.getSelectedDepartmentId(), PageRequest.of(0, 10));
      model.addAttribute("employees", employees.getContent());

      for (var employee : employees.getContent()) {
        List<AttendanceDTO> attendanceDTOS = attendanceService.getAttendancesByEmployeeIdAndDate(employee.getEmployeeId(), LocalDate.now());
        int workDays = Math.toIntExact(
          attendanceDTOS.stream()
            .filter(a -> "Đúng giờ".equals(a.getAttendanceStatus()))
            .count()
        );
        int lateDays = Math.toIntExact(
          attendanceDTOS.stream()
            .filter(a -> "Đi muộn".equals(a.getAttendanceStatus()))
            .count()
        );
        int absentDays = Math.toIntExact(
          attendanceDTOS.stream()
            .filter(a -> "Nghỉ".equals(a.getAttendanceStatus()))
            .count()
        );
        double overtimeHours = attendanceDTOS.stream()
          .filter(a -> a != null)
          .mapToDouble(a -> a.getAttendanceOvertimeHours() != null ?
            a.getAttendanceOvertimeHours() : 0.0)
          .sum();
        PayrollCalculationDTO dto = new PayrollCalculationDTO(
          employee.getEmployeeId(),
          employee.getEmployeeFirstName(),
          employee.getEmployeeLastName(),
          workDays, lateDays, absentDays, Math.floor(overtimeHours)
        );
        form.getPayrollCalculations().add(dto);
      }
    }
    model.addAttribute("departments", departmentService.getAllDepartments());
    model.addAttribute("user", loggedInUser);
    model.addAttribute("payrollCalculationForm", form);
    model.addAttribute("contentFragment", "fragments/attendance-summary-fragments");
    return "index";
  }

  @PostMapping("/summary/removeEmployee")
  public String removeEmployee(@RequestParam Integer employeeId,
                               @ModelAttribute PayrollCalculationForm form,
                               RedirectAttributes redirectAttributes) {
    log.info("Attempting to remove employee: {}", employeeId);

    boolean removed = form.getPayrollCalculations().removeIf(dto -> dto.employeeId().equals(employeeId));

    if (removed) {
      redirectAttributes.addFlashAttribute("success", "Đã xóa nhân viên thành công!");
    } else {
      redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên để xóa");
    }

    return "redirect:/attendance/summary";
  }

  @PostMapping("/summary/calculate")
  public String calculatePayroll(@Valid @ModelAttribute PayrollCalculationForm form,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 @ModelAttribute("loggedInUser") User loggedInUser) {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("error", "Cần chọn ít nhất 1 người để tổng hợp công");
      redirectAttributes.addFlashAttribute("payrollCalculationForm", form);
      return "redirect:/attendance/summary";
    }

    form.setRequesterId(loggedInUser.getUserId());
    salaryRecordService.savePayroll(form);
    return "redirect:/request";
  }


  @GetMapping("/export")
  public ResponseEntity<Resource> exportAttendanceToExcel(
          @RequestParam(value = "year", required = false, defaultValue = "all") String year,
          @RequestParam(value = "month", required = false, defaultValue = "all") String month,
          @RequestParam(value = "week", required = false, defaultValue = "all") String week,
          @RequestParam(value = "status", required = false, defaultValue = "all") String status,
          @RequestParam(value = "selectedAttendances", required = false) String selectedAttendanceIds
  ) {
    // Convert selectedAttendances to list
    List<Integer> attendanceIds = new ArrayList<>();
    if (selectedAttendanceIds != null && !selectedAttendanceIds.isEmpty()) {
      attendanceIds = Arrays.stream(selectedAttendanceIds.split(","))
              .map(Integer::parseInt)
              .collect(Collectors.toList());
    }

    // Default date range (start with wide date range)
    LocalDate startDate = LocalDate.of(2020, 1, 1);
    LocalDate endDate = LocalDate.now();

    // Apply date filters if provided - USE EXACTLY THE SAME LOGIC AS IN viewExportAttendance
    if (year != null && !year.equals("all")) {
      int yearValue = Integer.parseInt(year);
      startDate = LocalDate.of(yearValue, 1, 1);
      endDate = LocalDate.of(yearValue, 12, 31);

      if (month != null && !month.equals("all")) {
        int monthValue = Integer.parseInt(month);
        YearMonth yearMonth = YearMonth.of(yearValue, monthValue);
        startDate = yearMonth.atDay(1);
        endDate = yearMonth.atEndOfMonth();

        if (week != null && !week.equals("all")) {
          int weekValue = Integer.parseInt(week);
          // Fixed week calculation
          int daysInMonth = yearMonth.lengthOfMonth();
          int totalWeeks = (int) Math.ceil(daysInMonth / 7.0);

          if (weekValue <= totalWeeks) {
            // First day of the week (1-based week of month)
            int firstDayOfWeek = (weekValue - 1) * 7 + 1;
            startDate = yearMonth.atDay(Math.min(firstDayOfWeek, daysInMonth));

            // Last day of the week
            int lastDayOfWeek = firstDayOfWeek + 6;
            endDate = yearMonth.atDay(Math.min(lastDayOfWeek, daysInMonth));
          }
        }
      }
    }

    log.info("Exporting attendance with date range: {} to {}, status: {}", startDate, endDate, status);

    // Xử lý recordedOnly và status đúng cách
    boolean recordedOnly = status == null || status.equalsIgnoreCase("all");

    // Nếu status là "all", đặt nó thành null để service xử lý đúng
    String effectiveStatus = status != null && status.equalsIgnoreCase("all") ? null : status;

    Resource file = attendanceService.exportAttendanceToExcel(attendanceIds, startDate, endDate, effectiveStatus, recordedOnly);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
  }
  @RequestMapping("/export/view")
  public String viewExportAttendance(
          @RequestParam(value = "year", required = false, defaultValue = "all") String year,
          @RequestParam(value = "month", required = false, defaultValue = "all") String month,
          @RequestParam(value = "week", required = false, defaultValue = "all") String week,
          @RequestParam(value = "status", required = false, defaultValue = "all") String status,
          @RequestParam(value = "page", defaultValue = "0") Integer page,
          @RequestParam(value = "size", defaultValue = "10") Integer size,
          Model model) {

    LocalDate startDate = LocalDate.of(2020, 1, 1);
    LocalDate endDate = LocalDate.now();

    // Get years, months, and weeks for filter dropdowns
    List<Integer> years = attendanceService.getAllYears();
    List<Integer> months = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());

    // Calculate weeks dynamically based on selected month
    int weekCount = 4; // Default
    if (!month.equals("all") && !year.equals("all")) {
      int yearValue = Integer.parseInt(year);
      int monthValue = Integer.parseInt(month);
      YearMonth yearMonth = YearMonth.of(yearValue, monthValue);
      weekCount = (int) Math.ceil(yearMonth.lengthOfMonth() / 7.0);
    }
    List<Integer> weeks = IntStream.rangeClosed(1, weekCount).boxed().collect(Collectors.toList());

    // Apply date filters if provided
    if (year != null && !year.equals("all")) {
      int yearValue = Integer.parseInt(year);
      startDate = LocalDate.of(yearValue, 1, 1);
      endDate = LocalDate.of(yearValue, 12, 31);

      if (month != null && !month.equals("all")) {
        int monthValue = Integer.parseInt(month);
        YearMonth yearMonth = YearMonth.of(yearValue, monthValue);
        startDate = yearMonth.atDay(1);
        endDate = yearMonth.atEndOfMonth();

        if (week != null && !week.equals("all")) {
          int weekValue = Integer.parseInt(week);
          // Fixed week calculation
          int daysInMonth = yearMonth.lengthOfMonth();
          int totalWeeks = (int) Math.ceil(daysInMonth / 7.0);

          if (weekValue <= totalWeeks) {
            // First day of the week (1-based week of month)
            int firstDayOfWeek = (weekValue - 1) * 7 + 1;
            startDate = yearMonth.atDay(Math.min(firstDayOfWeek, daysInMonth));

            // Last day of the week
            int lastDayOfWeek = firstDayOfWeek + 6;
            endDate = yearMonth.atDay(Math.min(lastDayOfWeek, daysInMonth));
          }
        }
      }
    }

    Page<AttendanceDTO> attendances;
    // Only show attendance records that have been recorded
    if (status != null && !status.equals("all")) {
      attendances = attendanceService.getAll(startDate, endDate, status, PageRequest.of(page, size));
    } else {
      // Modified to only show recorded attendance (not empty records)
      attendances = attendanceService.getAllRecorded(startDate, endDate, PageRequest.of(page, size));
    }

    model.addAttribute("attendances", attendances);
    model.addAttribute("years", years);
    model.addAttribute("months", months);
    model.addAttribute("weeks", weeks);
    model.addAttribute("selectedYear", year);
    model.addAttribute("selectedMonth", month);
    model.addAttribute("selectedWeek", week);
    model.addAttribute("status", status);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("contentFragment", "fragments/attendance-export-fragments");
    return "index";
  }
  @RequestMapping("/status")
  public String status(@RequestParam(value = "month", required = false) String month,
                       @RequestParam(value = "year", required = false) String year,
                       @RequestParam(value = "action" ,required = false) String action,
                       @RequestParam(value = "query" ,required = false) String query,
                       Model model, Pageable pageable) {

    String formattedDate = (String) model.getAttribute("formattedDate");


    if (formattedDate == null || month != null || year != null) {
      if (month == null) month = "01";
      if (year == null) year = "2024";
      formattedDate = year + "-" + month;
      model.addAttribute("formattedDate", formattedDate);
    }

    if (action == null) {
      action = (String) model.getAttribute("action");
    }
    if (query == null) {
      query = (String) model.getAttribute("query");
    }


    List<Position> positions = positionService.getAllPositions();
    List<Department> departments = departmentService.getAllDepartments();
    List<EmployeeAttendanceStatusDTO> employeeAttendanceStatusDTOS = new ArrayList<>();
    System.out.println("4" + action);
    if("search".equals(action) && query != null && !query.trim().isEmpty()){
        employeeAttendanceStatusDTOS = attendanceService.findEmployeeAttendanceStatusbyEmployeeName(query, pageable, formattedDate);
      System.out.println(2);
      System.out.println("check date: " + formattedDate);
      System.out.println(employeeAttendanceStatusDTOS);
    }else if( "departmentfilter".equals(action) && query != null && !query.trim().isEmpty()){
      employeeAttendanceStatusDTOS = attendanceService.departmentFilter(query, pageable, formattedDate);
    }else {
      employeeAttendanceStatusDTOS = attendanceService.getEmployeeAttendanceStatus(formattedDate, pageable);
      System.out.println(3);
    }

    model.addAttribute("employeeAttendanceStatusDTOS", employeeAttendanceStatusDTOS);
    model.addAttribute("positions", positions);
    model.addAttribute("departments", departments);
    return "fragments/employee-attendance-status";
  }


  @GetMapping("/searchstatus")
  public String searchstatus(@RequestParam("query") String search,
                              RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("action", "search");
    redirectAttributes.addFlashAttribute("query", search);
    return "redirect:/attendance/status";

  }
  @GetMapping("/departmentfilter")
  public String departmentFilter(@RequestParam("value") String filter,
                                 RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("action", "departmentfilter");
    redirectAttributes.addFlashAttribute("query", filter);
    return "redirect:/attendance/status";
  }
}
