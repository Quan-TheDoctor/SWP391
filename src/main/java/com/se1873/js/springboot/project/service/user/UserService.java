package com.se1873.js.springboot.project.service.user;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.dto.RoleDTO;
import com.se1873.js.springboot.project.dto.UserDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.AuditLogDTOMapper;
import com.se1873.js.springboot.project.mapper.UserDTOMapper;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.AuditLogService;
import com.se1873.js.springboot.project.service.user.command.UserCommandService;
import com.se1873.js.springboot.project.service.user.query.UserQueryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {
  private final AuditLogDTOMapper auditLogDTOMapper;

  private final UserRepository userRepository;
  private final UserDTOMapper userDTOMapper;
  private final AuditLogService auditLogService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final UserCommandService userCommandService;
  private final UserQueryService userQueryService;

  public void createUserAccount(Employee employee) {
    userCommandService.createUserAccount(employee);
  }
  public User findUserByUserId(Integer userId) {
    return userQueryService.findUserByUserId(userId);
  }
  public Set<RoleDTO> getAllRoles() {
    List<User> users = userRepository.findAll();
    Set<RoleDTO> sets = new HashSet<>();

    for (User user : users) {
      RoleDTO role = new RoleDTO();
      role.setName(user.getRole());
      sets.add(role);
    }
    return sets;
  }
  @Transactional
  public void handleSuccessfulLogin(String username, HttpServletResponse response) throws IOException {
    com.se1873.js.springboot.project.entity.User userEntity =
      userQueryService.findUserByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    userEntity.setLastLogin(LocalDateTime.now());
    userCommandService.saveUser(userEntity);

    AuditLogDTO auditLogDTO = new AuditLogDTO();
    auditLogDTO.setUsername(username);
    auditLogDTO.setUser(userEntity);
    auditLogDTO.setCreatedAt(LocalDateTime.now());
    auditLogDTO.setActionInfo("Login successful");
    auditLogDTO.setActionType("Authentication");

    auditLogService.saveLog(auditLogDTOMapper.toEntity(auditLogDTO));
    boolean isAdmin = userEntity.getRole().equals("ADMIN");
    if (isAdmin) {
      response.sendRedirect("/homepage");
    } else {
      response.sendRedirect("/user/detail");
    }
  }
  public Page<UserDTO> getAll(Pageable pageable) {
    return userQueryService.getAll(pageable).map(userDTOMapper::toDTO);
  }
  public Optional<User> findUserByUsername(String username) {
    return userQueryService.findUserByUsername(username);
  }
  public UserDTO findByUserId(Integer id) {
    return userDTOMapper.toDTO(userQueryService.findUserByUserId(id));
  }
  public Page<UserDTO> searchUsers(String query, Pageable pageable) {
    return userQueryService.search(query, pageable).map(userDTOMapper::toDTO);
  }
  public Page<UserDTO> findByRole(String role, Pageable pageable) {
    return userQueryService.findByRole(role, pageable).map(userDTOMapper::toDTO);
  }
  public Page<UserDTO> findByStatus(String status, Pageable pageable) {
    return userQueryService.findByStatus(status, pageable).map(userDTOMapper::toDTO);
  }
  public void updateUser(UserDTO userDTO) {
    User user = userQueryService.findUserByUserId(userDTO.getUserId());
    user.setUsername(userDTO.getUsername());
    user.setRole(userDTO.getRole());

    userCommandService.saveUser(user);
  }

  public Integer countAllUsers() {
    return userQueryService.countAllUsers();
  }

  public Integer countByStatus(String status) {
    return userQueryService.countByStatus(status);
  }


  public Optional<User> getManagerByDepartmentId(Integer departmentId) {
    return userRepository.findManagerByDepartmentId(departmentId);
  }
}
