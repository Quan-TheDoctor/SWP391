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
import org.springframework.data.domain.Pageable;
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
import java.util.Map;
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

  Page<AttendanceDTO> attendances;

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

  @RequestMapping("/filter")
  public String filterStatus(Model model,
                             @RequestParam("status") String status,
                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                             @RequestParam(value = "size", defaultValue = "5") Integer size,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
    attendances = attendanceService.filterByStatus(PageRequest.of(page,size),status);
    if (startDate == null || endDate == null) {
      startDate = LocalDate.now();
      endDate = LocalDate.now();
    }

    Map<String,Integer> quantity = attendanceService.getQuantity();

    model.addAttribute("quantity",quantity);
    model.addAttribute("attendances",attendances);
    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
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
        double overtimeHours = attendanceDTOS.stream().mapToDouble(AttendanceDTO::getAttendanceOvertimeHours).sum();
        PayrollCalculationDTO dto = new PayrollCalculationDTO(
          employee.getEmployeeId(),
          employee.getEmployeeFirstName(),
          employee.getEmployeeLastName(),
          workDays, lateDays, absentDays, Math.floor(overtimeHours)
        );
        form.getPayrollCalculations().add(dto);
      }
    }
    model.addAttribute("user", user);
    model.addAttribute("payrollCalculationForm", form);
    return "attendance-summary";
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
    salaryRecordService.savePayroll(form);
    return "redirect:/request";
  }
  @GetMapping("/search")
  public String search(@RequestParam("query") String name, RedirectAttributes redirectAttributes, Pageable pageable) {
    attendances = attendanceService.searchAttendanceByEmployeeName(name, PageRequest.of(0,5));
    redirectAttributes.addFlashAttribute("attendances", attendances);

    return "redirect:/attendance";
  }
}
