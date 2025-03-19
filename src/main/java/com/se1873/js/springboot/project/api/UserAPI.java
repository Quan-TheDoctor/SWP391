package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user/")
public class UserAPI {
  private final UserService userService;

  @GetMapping("current")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    String username = ((UserDetails) authentication.getPrincipal()).getUsername();

    User user = userService.findUserByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return ResponseEntity.ok(user);
  }
}
