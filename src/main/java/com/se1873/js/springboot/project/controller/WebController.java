package com.se1873.js.springboot.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String home() {
        return "home"; // Trả về home.html
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Trả về dashboard.html (chỉ truy cập khi đăng nhập)
    }

    @GetMapping("/login")
    public String loginPage(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("errorMsg", "Tài khoản hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Bạn đã đăng xuất thành công!");
        }
        return "login"; // Trả về login.html
    }
    @RequestMapping("/homepage")
    public String homepage(Model model){
        return "homepage";
    }
}
