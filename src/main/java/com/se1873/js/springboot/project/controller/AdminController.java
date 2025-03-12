package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

  private final UserService userService;

  @GetMapping
  public String index(Model model) {
    var users = userService.getAll(PageRequest.of(0, 10));

    model.addAttribute("users", users);
    return "admin";
  }
}
