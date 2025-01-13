package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.service.PermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HelloController {
  private final PermissionService permissionService;

  public HelloController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @GetMapping("/")
  public String next(RedirectAttributes redirectAttributes, Model model) {
    Object loginStatus = model.getAttribute("loginStatus");
    if (loginStatus == null || !loginStatus.equals(true)) {
      return "redirect:/login";
    }
    model.addAttribute("test", "hahaha");
    return "index";
  }
}