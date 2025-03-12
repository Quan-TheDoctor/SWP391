package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.service.AuditLogService;
import com.se1873.js.springboot.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

  private final UserService userService;
  private final AuditLogService auditLogService;
  private final GlobalController globalController;

  @GetMapping("/logs")
  public String index(Model model,
                      @ModelAttribute("loggedInUser") User loggedInUser,
                      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    var auditLogs = auditLogService.getAll(pageable);

    globalController.createAuditLog(loggedInUser, "Navigate to /admin/ successfully", "Navigate");
    model.addAttribute("auditLogs", auditLogs);
    model.addAttribute("contentFragment", "fragments/admin-logs-fragments");
    return "index";
  }
}
