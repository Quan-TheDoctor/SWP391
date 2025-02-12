package com.se1873.js.springboot.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePasswordController {

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("errorMsg", "");
        model.addAttribute("successMsg", "");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        String currentPassword = "password";

        if (!oldPassword.equals(currentPassword)) {
            model.addAttribute("errorMsg", "Incorrect old password!");
            model.addAttribute("successMsg", "");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMsg", " New password does not match!");
            model.addAttribute("successMsg", "");
            return "change-password";
        }

        model.addAttribute("successMsg", "Password changed successfully!");
        model.addAttribute("errorMsg", "");
        return "change-password";
    }
}
