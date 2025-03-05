package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private SalaryRecordService salaryRecordService;

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

    @GetMapping("/detail")
    public String viewUserProfile(Model model) {
        EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(getEmployeeId());
        model.addAttribute("employeeDTO", employee);
        return "user";
    }

    @RequestMapping("/request/create")
    public String requestCreateForm(Model model) {

        model.addAttribute("requestDTO", new RequestDTO());
        return "user-request-create";
    }
    @RequestMapping("/attendance")
    public String attendance(Model model){
        Page<AttendanceDTO> attendanceDTO = attendanceService.getAttendanceByEmployeeId(getEmployeeId(), PageRequest.of(0,5));
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
                                   @RequestParam(value = "month", required = false) YearMonth month){
        Page<AttendanceDTO> attendanceDTO = null;
        if(month != null){
            Integer months = month.getMonthValue();
            Integer year = month.getYear();
            attendanceDTO = attendanceService.filterByMonth(PageRequest.of(0,5),months,year);
        }else if(status != null){
            attendanceDTO = attendanceService.filterByStatus(PageRequest.of(0,5),status);
        }
        model.addAttribute("attendanceDTO",attendanceDTO);
        return "user-attendance";
    }

    @RequestMapping("/filterPayroll")
    public String filterPayroll(Model model,
                                @RequestParam(value = "month", required = false) YearMonth month){
        if(month != null) {
            Integer months = month.getMonthValue();
            Integer year = month.getYear();
            Page<PayrollDTO> payrollDTO = salaryRecordService.filterByMonth(PageRequest.of(0, 5), months, year);
            model.addAttribute("payrollDTO", payrollDTO);
        }
        return "user-payroll";
    }

    @GetMapping("/download/{id}")
    public String dowloadInvoice(Model model,
                                 @PathVariable("id") int id){
        PayrollDTO payroll = salaryRecordService.payrollDTO(id);
        model.addAttribute("payroll", payroll);
        return "payroll-slip";
    }



}
