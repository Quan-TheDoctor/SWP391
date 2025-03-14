package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.AuditLogService;
import com.se1873.js.springboot.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {
  private final UserService userService;
  private final AuditLogService auditLogService;
  private final UserRepository userRepository;

  @ModelAttribute("servletPath")
  String getRequestServletPath(HttpServletRequest request) {
    return request.getServletPath();
  }

  @ModelAttribute("loggedInUser")
  User getUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      return null;
    }
    return userService.findUserByUsername(userDetails.getUsername())
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
  public void createAuditLog(User userRef, String action, String actionType, String level) {
    try {
      AuditLog auditLog = new AuditLog();
      auditLog.setUser(userRef);
      auditLog.setActionInfo(action);
      auditLog.setActionType(actionType);
      auditLog.setActionLevel(level);

      auditLogService.saveLog(auditLog);
    } catch (Exception e) {
      log.error("Error creating audit log: {}", e.getMessage());
    }
  }
}