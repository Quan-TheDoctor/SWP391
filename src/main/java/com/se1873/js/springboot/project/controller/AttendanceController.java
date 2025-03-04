package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.DepartmentService;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/attendance")
@SessionAttributes({"payrollCalculationForm", "user"})
public class AttendanceController {

  private final AttendanceService attendanceService;
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final SalaryRecordService salaryRecordService;
  private final UserRepository userRepository;
  private AttendanceDTOList attendanceDTOList = new AttendanceDTOList();

  @ModelAttribute("departments")
  public List<Department> getAllDepartments() {
    return departmentService.getAllDepartments();
  }

  @ModelAttribute("payrollCalculationForm")
  public PayrollCalculationForm payrollCalculationForm() {
    return new PayrollCalculationForm();
  }

  @ModelAttribute("user")
  public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
    if (userDetails == null) {
      return null;
    }
    User user = userRepository.findUserByUsername(userDetails.getUsername())
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    session.setAttribute("user", user);
    return user;
  }

  @RequestMapping
  public String attendance(Model model,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "size", defaultValue = "5") Integer size) {
    if (startDate == null || endDate == null) {
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
  public String searchAttendance(Model model) {
    return null;
  }

  @RequestMapping("/summary")
  public String showPage(@ModelAttribute PayrollCalculationForm form,
                         Model model,
                         @ModelAttribute("user") User user) {
    if (user == null) {
      return "redirect:/login";
    }

    if (form.getSelectedDepartmentId() != null) {
      Page<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(form.getSelectedDepartmentId(), PageRequest.of(0, 10));
      model.addAttribute("employees", employees.getContent());
    }
    model.addAttribute("user", user);
    model.addAttribute("payrollCalculationForm", form);
    return "attendance-summary";
  }

  @PostMapping("/summary/addEmployee")
  public String addEmployee(@RequestParam Integer employeeId,
                            @ModelAttribute PayrollCalculationForm form,
                            RedirectAttributes redirectAttributes) {
    log.info("Attempting to add employee: {}", employeeId);

    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(employeeId);

    boolean exists = form.getPayrollCalculations().stream()
      .anyMatch(dto -> dto.employeeId().equals(employeeId));

    if (!exists && employee != null) {
      List<AttendanceDTO> attendanceDTOS = attendanceService.getAttendancesByEmployeeIdAndDate(employeeId, LocalDate.now());
      log.info(attendanceDTOS.toString());
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

      double overtimeHours = attendanceDTOS.stream().mapToDouble(AttendanceDTO::getAttendanceOvertimeHours).sum();

      PayrollCalculationDTO dto = new PayrollCalculationDTO(
        employee.getEmployeeId(),
        employee.getEmployeeFirstName(),
        employee.getEmployeeLastName(),
        workDays, lateDays, absentDays, Math.floor(overtimeHours)
      );
      form.getPayrollCalculations().add(dto);
      redirectAttributes.addFlashAttribute("success", "Đã thêm nhân viên thành công!");
    } else {
      redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm nhân viên");
    }

    return "redirect:/attendance/summary";
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
                                 @ModelAttribute("user") User user) {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("error", "Cần chọn ít nhất 1 người để tổng hợp công");
      redirectAttributes.addFlashAttribute("payrollCalculationForm", form);
      return "redirect:/attendance/summary";
    }

    form.setRequesterId(user.getUserId());
    log.info(form.toString());
    salaryRecordService.savePayroll(form);
    return "redirect:/result";
  }
}
