package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.service.EmployeeService;
import com.se1873.js.springboot.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
