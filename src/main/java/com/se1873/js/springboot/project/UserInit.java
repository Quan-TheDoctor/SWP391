package com.se1873.js.springboot.project;

import com.se1873.js.springboot.project.entity.PermissionLevel;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.repository.RoleRepository;
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
  private final RoleRepository roleRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    List<User> user = userRepository.findUsersByUsername("admin");
    Role role = roleRepository.getRoleByName("Administrator");
    if(role == null) {
      role = new Role();
      role.setName("Administrator");
      role.setUserPermission(PermissionLevel.MANAGE);
      role.setPayrollPermission(PermissionLevel.MANAGE);
      role.setAttendancePermission(PermissionLevel.MANAGE);
      role.setRequestPermission(PermissionLevel.MANAGE);
      role.setEmployeePermission(PermissionLevel.MANAGE);
      role.setRecruitmentPermission(PermissionLevel.MANAGE);
      role.setSystemPermission(PermissionLevel.MANAGE);
      role.setDescription("Admin");

      role = roleRepository.save(role);
    }
    if(!user.isEmpty()) return;
    User admin = new User();
    admin.setUsername("admin");
    admin.setPasswordHash(passwordEncoder.encode("1"));
    admin.setRole(role.getName());
    userRepository.save(admin);
  }
}
