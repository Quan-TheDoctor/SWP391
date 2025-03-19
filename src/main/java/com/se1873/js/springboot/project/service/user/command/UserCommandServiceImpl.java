package com.se1873.js.springboot.project.service.user.command;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCommandServiceImpl implements UserCommandService {
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final UserRepository userRepository;

  public UserCommandServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.userRepository = userRepository;
  }

  @Override
  public void saveUser(User user) {
    userRepository.save(user);
  }

  @Override
  public void createUserAccount(Employee employee) {
    userRepository.save(User.builder()
      .username(employee.getCompanyEmail())
      .passwordHash(bCryptPasswordEncoder.encode("1"))
      .role("USER")
      .employee(employee)
      .build());
  }
}
