package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.model.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
  @GetMapping("/login")
  public String showLoginForm(Model model) {
    model.addAttribute("loginForm", new LoginForm());
    return "login";
  }

  @PostMapping("/login")
  public String handleLogin(@ModelAttribute LoginForm loginForm, Model model, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("loginStatus", true);
    return "redirect:/";
  }
}
