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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/detail")
    public String viewUserProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findUserByUsername(username);
            int employeeId = user.get().getEmployee().getEmployeeId();
            EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(employeeId);
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        Optional<User> user = userRepository.findUserByUsername(userName);
        if(user.isPresent()){
        Integer employeeId = user.get().getEmployee().getEmployeeId();
        Page<AttendanceDTO> attendanceDTO = attendanceService.getAttendanceByEmployeeId(employeeId, PageRequest.of(0,5));
        model.addAttribute("attendanceDTO",attendanceDTO);
        }
        return "user-attendance";
    }

    @RequestMapping("/payroll")
    public String payroll(Model model){
        Authentication authencation = SecurityContextHolder.getContext().getAuthentication();
        String userName = authencation.getName();
        Optional<User> user = userRepository.findUserByUsername(userName);
        Integer employeeId = user.get().getEmployee().getEmployeeId();
//        Page<PayrollDTO> payrollDTO = salaryRecordService.getPayrollByEmployeeId(employeeId,PageRequest.of(0,5));
//        model.addAttribute("payrollDTO",payrollDTO);
        return "user-payroll";
    }
}
