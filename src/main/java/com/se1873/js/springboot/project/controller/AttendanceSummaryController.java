package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.dto.PayrollCalculationDTO;
import com.se1873.js.springboot.project.dto.PayrollCalculationForm;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/attendance/summary")
public class AttendanceSummaryController {
  private final EmployeeService employeeService;
  private final AttendanceService attendanceService;
  private final DepartmentService departmentService;
  private final SalaryRecordService salaryRecordService;

  public AttendanceSummaryController(EmployeeService employeeService, AttendanceService attendanceService, DepartmentService departmentService, SalaryRecordService salaryRecordService) {
    this.employeeService = employeeService;
    this.attendanceService = attendanceService;
    this.departmentService = departmentService;
    this.salaryRecordService = salaryRecordService;
  }

  @RequestMapping("/filter")
  public String applyFilters(
    @RequestParam Integer selectedDepartmentId,
    @RequestParam Integer selectedMonth,
    @RequestParam Integer selectedYear,
    @RequestParam Integer pageSize,
    @RequestParam(value = "page", defaultValue = "0") Integer page,
    @RequestParam(value = "search", required = false) String searchQuery) {

    StringBuilder redirectUrl = new StringBuilder("/attendance/summary?");
    redirectUrl.append("selectedDepartmentId=").append(selectedDepartmentId)
              .append("&selectedMonth=").append(selectedMonth)
              .append("&selectedYear=").append(selectedYear)
              .append("&pageSize=").append(pageSize)
              .append("&page=").append(page);

    if (searchQuery != null && !searchQuery.isEmpty()) {
      redirectUrl.append("&search=").append(searchQuery);
    }

    return "redirect:" + redirectUrl.toString();
  }

  @RequestMapping
  public String summaryPage(@ModelAttribute PayrollCalculationForm form,
                            Model model,
                            @ModelAttribute("loggedInUser") User loggedInUser,
                            @RequestParam(value = "selectedDepartmentId", required = false) Integer selectedDepartmentId,
                            @RequestParam(value = "selectedMonth", required = false) Integer selectedMonth,
                            @RequestParam(value = "selectedYear", required = false) Integer selectedYear,
                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                            @RequestParam(value = "search", required = false) String searchQuery) {
    List<EmployeeDTO> employees = new ArrayList<>();

    if(selectedDepartmentId != null) {
      form.getPayrollCalculations().clear();

      if (selectedDepartmentId == 0) {
        employees = employeeService.getAllEmployees();
      } else {
        employees = employeeService.getEmployeesByDepartmentId(selectedDepartmentId);
      }

      if (searchQuery != null && !searchQuery.trim().isEmpty()) {
        String query = searchQuery.toLowerCase();
        employees = employees.stream()
          .filter(e -> (e.getEmployeeFirstName() + " " + e.getEmployeeLastName()).toLowerCase().contains(query))
          .collect(Collectors.toList());
      }


      if(selectedMonth == null) {
        selectedMonth = LocalDate.now().getMonthValue();
      }

      if(selectedYear == null) {
        selectedYear = LocalDate.now().getYear();
      }

      List<PayrollCalculationDTO> allCalculations = new ArrayList<>();

      for(EmployeeDTO employee : employees) {
        List<AttendanceDTO> attendanceDTOS = attendanceService.getAttendancesOfEmployeeIdByMonthAndYear(employee.getEmployeeId(), selectedMonth, selectedYear);

        Integer onTimeCounts = attendanceService.countAttendanceByStatus(attendanceDTOS, "On time");
        Integer lateCounts = attendanceService.countAttendanceByStatus(attendanceDTOS, "Late");
        Integer absentCounts = attendanceService.countAttendanceByStatus(attendanceDTOS, "Absent");
        double overtimeHours = attendanceService.countOvertimeHours(attendanceDTOS);

        PayrollCalculationDTO dto = PayrollCalculationDTO.builder()
          .employeeId(employee.getEmployeeId())
          .employeeFistName(employee.getEmployeeFirstName())
          .employeeLastName(employee.getEmployeeLastName())
          .workedDays(onTimeCounts)
          .lateDays(lateCounts)
          .absentDays(absentCounts)
          .overtimeHours(Math.floor(overtimeHours))
          .build();

        allCalculations.add(dto);
      }

      int totalItems = allCalculations.size();
      int totalPages = (int) Math.ceil((double) totalItems / pageSize);

      if (page >= totalPages && totalPages > 0) {
        page = totalPages - 1;
      }

      int startIndex = page * pageSize;
      int endIndex = Math.min(startIndex + pageSize, totalItems);

      List<PayrollCalculationDTO> pagedCalculations =
        totalItems > 0 ? allCalculations.subList(startIndex, endIndex) : new ArrayList<>();

      form.getPayrollCalculations().addAll(pagedCalculations);

      model.addAttribute("currentPage", page);
      model.addAttribute("totalPages", totalPages);
      model.addAttribute("totalItems", totalItems);
      model.addAttribute("pageSize", pageSize);
      model.addAttribute("searchQuery", searchQuery);
    }

    model.addAttribute("employees", employees);
    model.addAttribute("departments", departmentService.getAllDepartments());
    model.addAttribute("selectedMonth", selectedMonth);
    model.addAttribute("selectedYear", selectedYear);
    model.addAttribute("user", loggedInUser);
    model.addAttribute("payrollCalculationForm", form);
    model.addAttribute("contentFragment", "fragments/attendance-summary-fragments");
    return "index";
  }
  @PostMapping("/removeEmployee")
  public String removeEmployee(@RequestParam Integer employeeId,
                               @ModelAttribute PayrollCalculationForm form,
                               RedirectAttributes redirectAttributes) {
    boolean removed = form.getPayrollCalculations().removeIf(dto -> dto.getEmployeeId().equals(employeeId));

    if (removed) {
      redirectAttributes.addFlashAttribute("success", "Đã xóa nhân viên thành công!");
    } else {
      redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên để xóa");
    }

    return "redirect:/attendance/summary";
  }

  @PostMapping("/calculate")
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
}
