package com.se1873.js.springboot.project;

import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserInit implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    List<User> user = userRepository.findUsersByUsername("admin");
    if(!user.isEmpty()) return;
    User admin = new User();
    admin.setUsername("admin");
    admin.setPasswordHash(passwordEncoder.encode("1"));
    admin.setRole("ADMIN");
    userRepository.save(admin);
  }
}
