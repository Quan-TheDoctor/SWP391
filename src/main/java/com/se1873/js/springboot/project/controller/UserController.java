package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.LeavePolicyRepository;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.repository.NotificationRepository;
import com.se1873.js.springboot.project.service.*;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private LeavePolicyRepository leavePolicyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private SalaryRecordService salaryRecordService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RequestService requestService;
    @Autowired
    private LeaveRepository leaveRepository;

    public Integer getEmployeeId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findUserByUsername(username);
        if(user.isPresent()) {
            return user.get().getEmployee().getEmployeeId();
        }else{
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
    public String viewUserProfile(Model model) {
        EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());
        List<Notification> notification = notificationRepository.getNotificationsByUser_UserId(getUserId());
        long unreadCount = notification.stream().filter(n -> "unread".equals(n.getStatus())).count();
        model.addAttribute("unreadCount",unreadCount);
        model.addAttribute("employeeDTO", employee);
        return "user";
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

        return "user-request-create";
    }



    @RequestMapping("/attendance")
    public String attendance(Model model){
        Page<AttendanceDTO> attendanceDTO = attendanceService.getAttendanceByEmployeeId(getEmployeeId(),
                                                                                        PageRequest.of(0,5));
        Map<String,Integer> quantity = attendanceService.getQuantityEmployeeDetail(getEmployeeId());

        model.addAttribute("quantity",quantity);
        model.addAttribute("attendanceDTO",attendanceDTO);
        return "user-attendance";
    }

    @RequestMapping("/payroll")
    public String payroll(Model model){
        Page<PayrollDTO> payrollDTO = salaryRecordService.getPayrollByEmployeeId(PageRequest.of(0,5),getEmployeeId());
        model.addAttribute("payrollDTO",payrollDTO);
        return "user-payroll";
    }

    @RequestMapping("/back")
    public String back(Model model){
        EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());
        model.addAttribute("employeeDTO", employee);
        return "user";
    }

    @RequestMapping("/filterAttendance")
    public String filterAttendance(Model model,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "month", required = false) YearMonth month) {
        Page<AttendanceDTO> attendanceDTO = null;

        if (month != null) {
            Integer months = month.getMonthValue();
            Integer year = month.getYear();
            attendanceDTO = attendanceService.filterByMonth(PageRequest.of(0, 5), getEmployeeId(), months, year);
        } else if (status != null) {
            attendanceDTO = attendanceService.filterByStatusAndEmployeeId(PageRequest.of(0, 5), status ,getEmployeeId());
            log.info(String.valueOf(getEmployeeId()));
        }

        Map<String,Integer> quantity = attendanceService.getQuantityEmployeeDetail(getEmployeeId());

        model.addAttribute("quantity",quantity);
        model.addAttribute("attendanceDTO", attendanceDTO);
        model.addAttribute("month",month != null ? month.toString() : "");
        model.addAttribute("status",status);
        return "user-attendance";
    }

    @RequestMapping("/filterPayroll")
    public String filterPayroll(Model model,
                                @RequestParam(value = "month", required = false) YearMonth month) {
        Page<PayrollDTO> payrollDTO ;

        if (month != null) {
            Integer months = month.getMonthValue();
            Integer year = month.getYear();
            payrollDTO = salaryRecordService.filterByMonth(PageRequest.of(0, 5), getEmployeeId(), months, year);
            model.addAttribute("month", month.toString());
        } else {
            model.addAttribute("month", "");
            payrollDTO = salaryRecordService.filterByMonth(PageRequest.of(0, 5),null, null ,getEmployeeId() );
        }

        model.addAttribute("payrollDTO", payrollDTO);
        return "user-payroll";
    }



    @GetMapping("/download/{id}")
    public String dowloadInvoice(Model model,
                                 @PathVariable("id") int id){
        PayrollDTO payroll = salaryRecordService.payrollDTO(id);
        model.addAttribute("payroll", payroll);
        return "payroll-slip";
    }

    @RequestMapping("/notification")
    public String notification(Model model){
        List<Notification> notifications = notificationRepository.getNotificationsByUser_UserId(getUserId());
        long unreadCount = notifications.stream().filter(n -> "unread".equals(n.getStatus())).count();
        model.addAttribute("unreadCount",unreadCount);
        model.addAttribute("notifications",notifications);
        return "notification";
    }

    @RequestMapping("/mark")
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
        return "notification";
    }

    @GetMapping("/view/{notificationId}")
    public String view(Model model,
                       @PathVariable("notificationId") Integer notificationId){
        Notification notification = notificationRepository.getNotificationByNotificationId(notificationId);
        Integer requestId = notification.getRequestId();
        RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
        model.addAttribute("requestDTO", requestDTO);
        return "notificationDetail";
    }

    @RequestMapping("/backNotification")
    public String backNotification(Model model){
        List<Notification> notifications = notificationRepository.getNotificationsByUser_UserId(getUserId());
        long unreadCount = notifications.stream().filter(n -> "unread".equals(n.getStatus())).count();
        model.addAttribute("unreadCount",unreadCount);
        model.addAttribute("notifications",notifications);
        return "notification";
    }

}
