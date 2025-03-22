package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.LeavePolicyRepository;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.LeavePolicyService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import com.se1873.js.springboot.project.service.user.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {
  private final UserService userService;
  private final EmployeeService employeeService;
  private final AttendanceService attendanceService;
  private final SalaryRecordService salaryRecordService;
  private final LeavePolicyService leavePolicyService;
  private final LeavePolicyRepository leavePolicyRepository;
  private final LeaveRepository leaveRepository;
  private final AttendanceRepository attendanceRepository;

  public UserController(UserService userService, EmployeeService employeeService, AttendanceService attendanceService, SalaryRecordService salaryRecordService, LeavePolicyService leavePolicyService, LeavePolicyRepository leavePolicyRepository, LeaveRepository leaveRepository, AttendanceRepository attendanceRepository) {
    this.userService = userService;
    this.employeeService = employeeService;
    this.attendanceService = attendanceService;
    this.salaryRecordService = salaryRecordService;
    this.leavePolicyService = leavePolicyService;
    this.leavePolicyRepository = leavePolicyRepository;
    this.leaveRepository = leaveRepository;
    this.attendanceRepository = attendanceRepository;
  }

  public Integer getEmployeeId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Optional<User> user = userService.findUserByUsername(username);
    if (user.isPresent()) {
      return user.get().getEmployee().getEmployeeId();
    } else {
      throw new UsernameNotFoundException("not found user");
    }
  }

  @GetMapping("/detail")
  public String viewUserProfile(Model model) {
    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());

    model.addAttribute("employeeDTO", employee);
    model.addAttribute("contentFragment", "fragments/user-fragments");
    return "index";
  }

  @RequestMapping("/save")
  public String saveUserProfile(
    @Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
    @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
    BindingResult result,
    Model model) throws IOException {
    if (avatarFile != null && !avatarFile.isEmpty()) {
      log.info("test");
      employeeDTO.setPicture(avatarFile.getBytes());
    }

    if (result.hasErrors()) {

      model.addAttribute("message", result.getAllErrors().get(0).getDefaultMessage());
      model.addAttribute("messageType", "error");
      model.addAttribute("contentFragment", "fragments/user-fragments");
      return "index";
    }

    try {
      log.info(employeeDTO.toString());
      employeeService.saveEmployee(employeeDTO);
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
      model.addAttribute("messageType", "error");
      model.addAttribute("contentFragment", "fragments/user-fragments");
      return "index";
    }

    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());

    model.addAttribute("message", "Updated profile successfully");
    model.addAttribute("employeeDTO", employee);
    model.addAttribute("contentFragment", "fragments/user-fragments");
    return "index";
  }

  @RequestMapping("/request/create")
  public String requestCreateForm(Model model,
                                  @RequestParam(value = "reason", required = false) Integer reason) {
    List<LeavePolicy> leavePolicy = leavePolicyRepository.findAll();
    RequestDTO requestDTO = new RequestDTO();
    requestDTO.setLeaveDTO(LeaveDTO.builder().leaveAllowedDay(0).build());

    if (reason != null) {
      LeavePolicy selectedPolicy = leavePolicyRepository.findLeavePolicyByLeavePolicyId(reason);
      if (selectedPolicy != null) {
        //                int totalUsedDays = leaveRepository.findAllByEmployee_EmployeeId(getEmployeeId()).stream()
        //                        .filter(leave -> leave.getReason().equals(selectedPolicy.getLeavePolicyName()))
        //                        .mapToInt(Leave::getTotalDays)
        //                        .sum();
        //                log.info(String.valueOf(totalUsedDays));
        //                int remainingDays = selectedPolicy.getLeavePolicyAmount() - totalUsedDays;
        //                requestDTO.getLeaveDTO().setLeaveAllowedDay(remainingDays);
        Leave leave = leaveRepository.findTopByEmployee_EmployeeIdAndReasonOrderByLeaveIdDesc(getEmployeeId(), selectedPolicy.getLeavePolicyName());
        int remainingDays = leave != null ? leave.getLeaveAllowedDay() : selectedPolicy.getLeavePolicyAmount();
        requestDTO.getLeaveDTO().setLeaveAllowedDay(remainingDays);
        log.info(String.valueOf(remainingDays));
      }
    }
    model.addAttribute("leavePolicy", leavePolicy);
    model.addAttribute("requestDTO", requestDTO);
    model.addAttribute("reason", reason);

    model.addAttribute("requestDTO", new RequestDTO());
    model.addAttribute("contentFragment", "fragments/user-request-create-fragments");
    return "index";
  }

  @RequestMapping("/attendance")
  public String attendance(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "5") int size) {
    Page<AttendanceDTO> attendanceDTO = attendanceService.getAttendanceByEmployeeId(getEmployeeId(),
      PageRequest.of(page, size));
    Map<String, Integer> quantity = attendanceService.getQuantityEmployeeDetail(getEmployeeId());

    model.addAttribute("quantity", quantity);
    model.addAttribute("attendanceDTO", attendanceDTO);
    model.addAttribute("contentFragment", "fragments/user-attendance-fragments");
    return "index";
  }

  @RequestMapping("/payroll")
  public String payroll(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {
    Page<PayrollDTO> payrollDTO = salaryRecordService.getPayrollByEmployeeId(PageRequest.of(page, size), getEmployeeId());
    model.addAttribute("payrollDTO", payrollDTO);
    model.addAttribute("contentFragment", "fragments/user-payroll-fragments");
    return "index";
  }

  @RequestMapping("/back")
  public String back(Model model) {
    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());
    model.addAttribute("employeeDTO", employee);
    model.addAttribute("contentFragment", "fragments/user-fragments");
    return "index";
  }

  @RequestMapping("/filterAttendance")
  public String filterAttendance(Model model,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "month", required = false) YearMonth month,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "5") int size) {
    Page<AttendanceDTO> attendanceDTO = null;

    if (month != null) {
      Integer months = month.getMonthValue();
      Integer year = month.getYear();
      attendanceDTO = attendanceService.filterByMonth(PageRequest.of(page, size), getEmployeeId(), months, year);
    } else if (status != null) {
      attendanceDTO = attendanceService.filterByStatusAndEmployeeId(PageRequest.of(page, size), status, getEmployeeId());
      log.info(String.valueOf(getEmployeeId()));
    }

    Map<String, Integer> quantity = attendanceService.getQuantityEmployeeDetail(getEmployeeId());

    model.addAttribute("quantity", quantity);
    model.addAttribute("attendanceDTO", attendanceDTO);
    model.addAttribute("month", month != null ? month.toString() : "");
    model.addAttribute("contentFragment", "fragments/user-attendance-fragments");
    return "index";
  }

  @RequestMapping("/filterPayroll")
  public String filterPayroll(Model model,
                              @RequestParam(value = "month", required = false) YearMonth month,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "5") int size) {
    if (month != null) {
      Integer months = month.getMonthValue();
      Integer year = month.getYear();
      Page<PayrollDTO> payrollDTO = salaryRecordService.filterByMonth(PageRequest.of(page, size), getEmployeeId(), months, year);
      model.addAttribute("payrollDTO", payrollDTO);
      model.addAttribute("month", month != null ? month.toString() : "");
    }
    model.addAttribute("contentFragment", "fragments/user-payroll-fragments");
    return "index";
  }


  @GetMapping("/download/{id}")
  public String downloadPayslip(Model model,
                                @PathVariable("id") int id) {
    PayrollDTO payroll = salaryRecordService.payrollDTO(id);
    model.addAttribute("payroll", payroll);
    return "payroll-slip";
  }


}
