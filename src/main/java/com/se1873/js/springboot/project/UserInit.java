package com.se1873.js.springboot.project;

import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserInit implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    User user = new User();
    user.setUsername("admin");
    user.setPasswordHash(passwordEncoder.encode("1"));
    user.setRole("ADMIN");
    userRepository.save(user);
  }
}
