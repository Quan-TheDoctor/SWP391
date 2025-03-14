package com.se1873.js.springboot.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
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
    public String homepage(Model model){
        model.addAttribute("contentFragment", "fragments/homepage-fragments");
        return "index";
    }
}
