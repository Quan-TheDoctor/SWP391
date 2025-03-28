package com.se1873.js.springboot.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("contentFragment", "fragments/access-denied-fragments");
        return "index";
    }
} 