package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.LeavePolicyService;
import com.se1873.js.springboot.project.service.NotificationService;
import com.se1873.js.springboot.project.service.RequestService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.role.RoleService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.se1873.js.springboot.project.service.user.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final RequestService requestService;
  private final NotificationService notificationService;
  private final RoleService roleService;

  public UserController(UserService userService, EmployeeService employeeService, AttendanceService attendanceService, SalaryRecordService salaryRecordService, LeavePolicyService leavePolicyService, LeavePolicyRepository leavePolicyRepository, LeaveRepository leaveRepository, AttendanceRepository attendanceRepository, NotificationRepository notificationRepository, UserRepository userRepository, RequestService requestService, NotificationService notificationService, RoleService roleService) {
    this.userService = userService;
    this.employeeService = employeeService;
    this.attendanceService = attendanceService;
    this.salaryRecordService = salaryRecordService;
    this.leavePolicyService = leavePolicyService;
    this.leavePolicyRepository = leavePolicyRepository;
    this.leaveRepository = leaveRepository;
    this.attendanceRepository = attendanceRepository;
    this.notificationRepository = notificationRepository;
    this.userRepository = userRepository;
    this.requestService = requestService;
    this.notificationService = notificationService;
    this.roleService = roleService;
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

  public Integer getUserId(){
    Authentication authencation = SecurityContextHolder.getContext().getAuthentication();
    String userName = authencation.getName();
    Optional<User> user = userRepository.findUserByUsername(userName);
    if(user.isPresent()) {
      return user.get().getUserId();
    }else{
      throw new UsernameNotFoundException("not found user");
    }
  }

  @GetMapping("/detail")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String viewUserProfile(Model model) {
    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());

    List<Notification> notification = notificationRepository.getNotificationsByUser_UserId(getUserId());
    long unreadCount = notification.stream().filter(n -> "unread".equals(n.getStatus())).count();
    model.addAttribute("unreadCount",unreadCount);
    model.addAttribute("employeeDTO", employee);
    model.addAttribute("contentFragment", "fragments/user-fragments");
    return "index";
  }

  @RequestMapping("/save")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String saveUserProfile(
    @Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
    @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
    BindingResult result,
    Model model) throws IOException {
    if (avatarFile != null && !avatarFile.isEmpty()) {
      employeeDTO.setPicture(avatarFile.getBytes());
    }

    if (result.hasErrors()) {

      model.addAttribute("message", result.getAllErrors().get(0).getDefaultMessage());
      model.addAttribute("messageType", "error");
      model.addAttribute("contentFragment", "fragments/user-fragments");
      return "index";
    }

    try {
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
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String requestCreateForm(Model model,
                                  @RequestParam(value = "reason", required = false) Integer reason) {
    List<LeavePolicy> leavePolicy = leavePolicyRepository.findAll();
    RequestDTO requestDTO = new RequestDTO();
    requestDTO.setLeaveDTO(LeaveDTO.builder().leaveAllowedDay(0).build());

    if (reason != null) {
      LeavePolicy selectedPolicy = leavePolicyRepository.findLeavePolicyByLeavePolicyId(reason);
      if (selectedPolicy != null) {
        Leave leave = leaveRepository.findTopByEmployee_EmployeeIdAndReasonOrderByLeaveIdDesc(getEmployeeId(), selectedPolicy.getLeavePolicyName());
        int remainingDays = leave != null ? leave.getLeaveAllowedDay() : selectedPolicy.getLeavePolicyAmount();
        requestDTO.getLeaveDTO().setLeaveAllowedDay(remainingDays);
      }
    }
    model.addAttribute("leavePolicy", leavePolicy);
    model.addAttribute("requestDTO", requestDTO);
    model.addAttribute("reason", reason);
    model.addAttribute("contentFragment", "fragments/user-request-create-fragments");
    return "index";
  }

  @RequestMapping("/attendance")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
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
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String payroll(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {
    Page<PayrollDTO> payrollDTO = salaryRecordService.getPayrollByEmployeeId(PageRequest.of(page, size), getEmployeeId());
    model.addAttribute("payrollDTO", payrollDTO);
    model.addAttribute("contentFragment", "fragments/user-payroll-fragments");
    return "index";
  }

  @RequestMapping("/back")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String back(Model model) {
    EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());
    model.addAttribute("employeeDTO", employee);
    model.addAttribute("contentFragment", "fragments/user-fragments");
    return "index";
  }

  @RequestMapping("/filterAttendance")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
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
    }

    Map<String, Integer> quantity = attendanceService.getQuantityEmployeeDetail(getEmployeeId());

    model.addAttribute("quantity", quantity);
    model.addAttribute("attendanceDTO", attendanceDTO);
    model.addAttribute("month", month != null ? month.toString() : "");
    model.addAttribute("contentFragment", "fragments/user-attendance-fragments");
    return "index";
  }

  @RequestMapping("/filterPayroll")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
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
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String downloadPayslip(Model model,
                                @PathVariable("id") int id) {
    PayrollDTO payroll = salaryRecordService.payrollDTO(id);
    model.addAttribute("payroll", payroll);
    return "payroll-slip";
  }

  @RequestMapping("/notification")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String notification(Model model){
    List<Notification> notifications = notificationRepository.getNotificationsByUser_UserId(getUserId());
    long unreadCount = notifications.stream().filter(n -> "unread".equals(n.getStatus())).count();
    model.addAttribute("unreadCount",unreadCount);
    model.addAttribute("notifications",notifications);
    model.addAttribute("contentFragment", "fragments/notification");
    return "index";
  }

  @RequestMapping("/mark")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String mark(Model model,
                     @RequestParam("notificationId") Integer notificationId){
    Notification notification = notificationRepository.getNotificationByNotificationId(notificationId);
    String status = notification.getStatus();
    if("unread".equals(status)){
      String updateStatus = "read";
      notificationService.updateStatus(updateStatus,notificationId);
    }
    List<Notification> notifications = notificationRepository.getNotificationsByUser_UserId(getUserId());
    long unreadCount = notifications.stream().filter(n -> "unread".equals(n.getStatus())).count();
    model.addAttribute("unreadCount",unreadCount);
    model.addAttribute("notifications",notifications);
    model.addAttribute("contentFragment", "fragments/notification");
    return "index";
  }

  @GetMapping("/view/{notificationId}")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String view(Model model,
                     @PathVariable("notificationId") Integer notificationId){
    Notification notification = notificationRepository.getNotificationByNotificationId(notificationId);
    Integer requestId = notification.getRequestId();
    RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
    model.addAttribute("requestDTO", requestDTO);
    model.addAttribute("contentFragment", "fragments/notificationDetail");
    return "index";
  }

  @RequestMapping("/backNotification")
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String backNotification(Model model){
    List<Notification> notifications = notificationRepository.getNotificationsByUser_UserId(getUserId());
    long unreadCount = notifications.stream().filter(n -> "unread".equals(n.getStatus())).count();
    model.addAttribute("unreadCount",unreadCount);
    model.addAttribute("notifications",notifications);
    model.addAttribute("contentFragment", "fragments/notification");
    return "index";
  }
}
