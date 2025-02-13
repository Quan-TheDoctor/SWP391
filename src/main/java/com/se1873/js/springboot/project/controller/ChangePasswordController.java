package com.se1873.js.springboot.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePasswordController {

    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {

        String storedPassword = "admin123";

        if (!currentPassword.equals(storedPassword)) {
            model.addAttribute("errorMsg", "Current password is incorrect!");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "New passwords do not match!");
            return "change-password";
        }

        model.addAttribute("successMsg", "Password updated successfully!");
        return "change-password";
    }
}
