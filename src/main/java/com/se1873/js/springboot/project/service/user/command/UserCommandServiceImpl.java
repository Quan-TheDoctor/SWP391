package com.se1873.js.springboot.project.service.user.command;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.repository.RoleRepository;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCommandServiceImpl implements UserCommandService {
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  public UserCommandServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, RoleRepository roleRepository) {
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public void saveUser(User user) {
    userRepository.save(user);
  }

  @Override
  public void createUserAccount(Employee employee) {
    Role role = roleRepository.getRoleByName("Employee");

    userRepository.save(User.builder()
      .username(employee.getCompanyEmail())
      .passwordHash(bCryptPasswordEncoder.encode("1"))
      .role(role.getName())
      .employee(employee)
        .status("Active")
      .build());
  }
}
