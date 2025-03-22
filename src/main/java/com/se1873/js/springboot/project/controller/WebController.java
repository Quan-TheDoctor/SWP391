package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AttendanceCountDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.dto.TopSalaryDTO;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class WebController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private SalaryRecordService salaryRecordService;


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("contentFragment", "fragments/homepage-fragments");
        return "index";
    }

    @GetMapping("/face-recognition")
    public String faceRecognitionPage(Model model) {
        model.addAttribute("contentFragment", "fragments/face-recognition-fragments");
        return "index";
    }
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/login")
    public String loginPage(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("errorMsg", "Tài khoản hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Bạn đã đăng xuất thành công!");
        }
        return "login";
    }


    @RequestMapping("/homepage")
    public String homepage(Model model, @RequestParam(value = "month", required = false) String MonthAndYear,
                                        @RequestParam(value = "date", required = false) String date) {
        int totalemployee = employeeService.countEmployees();
        LocalDate today = LocalDate.now();
        LocalDate dateChoosed = (date != null) ? LocalDate.parse(date) : today;

        int year, month;
        if (MonthAndYear != null) {
            String[] time = MonthAndYear.split("-");
            year = Integer.parseInt(time[0]);
            month = Integer.parseInt(time[1]);
        } else {
            year = today.getYear();
            month = today.getMonthValue();
        }
        EmployeeCountDTO employeeCountDTO = employeeService.getAvailableAndUnavailableEmployeeCount();
        AttendanceCountDTO attendanceCountDTO = attendanceService.countAvailableAttendance(String.valueOf(dateChoosed));
        double avalableEmployeePercent = (employeeCountDTO.getTotalAvailableEmployees() *100) / totalemployee;
        double unavalableEmployeePercent = 100 - avalableEmployeePercent;

        List<TopSalaryDTO> Top3salaryDTOS = salaryRecordService.getTop3HighestNetSalary(month, year);
        int countAttendance = attendanceService.countDailyAttendance(today);

        model.addAttribute("avalableEmployeePercent", 90);
        System.out.println(avalableEmployeePercent);
        model.addAttribute("unavalableEmployeePercent", 10);
        model.addAttribute("Top3salaryDTOS", Top3salaryDTOS);
        model.addAttribute("attendanceCountDTO", attendanceCountDTO);
        model.addAttribute("countAttendance", countAttendance);
        model.addAttribute("totalemployee", totalemployee);
        model.addAttribute("contentFragment", "fragments/homepage-fragments");
        return "index";
    }
}
