package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.dto.RoleDTO;
import com.se1873.js.springboot.project.dto.UserDTO;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.AuditLogDTOMapper;
import com.se1873.js.springboot.project.mapper.UserDTOMapper;
import com.se1873.js.springboot.project.repository.UserRepository;
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
      userRepository.findUserByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    userEntity.setLastLogin(LocalDateTime.now());
    userRepository.save(userEntity);

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
    return userRepository.findAll(pageable).map(userDTOMapper::toDTO);
  }

  public Optional<User> findUserByUsername(String username) {
    return userRepository.findUserByUsername(username);
  }

  public UserDTO findById(Integer id) {
    User user = userRepository.findByUserId(id)
      .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    return userDTOMapper.toDTO(user);
  }


  public Page<UserDTO> searchUsers(String query, Pageable pageable) {
    return userRepository.findByUsernameContainingIgnoreCase(query, pageable).map(userDTOMapper::toDTO);
  }

  public Page<UserDTO> findByRole(String role, Pageable pageable) {
    return userRepository.findByRole(role, pageable).map(userDTOMapper::toDTO);
  }

  public Page<UserDTO> findByStatus(String status, Pageable pageable) {
    return userRepository.findByStatus(status, pageable).map(userDTOMapper::toDTO);
  }
  @Transactional
  public User lockUser(Integer userId, String reason) {
    User user = userRepository.findByUserId(userId)
      .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    user.setStatus("locked");
    return userRepository.save(user);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void lockUserById(Integer userId) {
    userRepository.updateUserStatus(userId, "locked");
  }
  public void unlockUser(Integer userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    user.setStatus("Active");

    userRepository.save(user);
  }

  public void updateUser(UserDTO userDTO) {
    User user = userRepository.findUserByUserId(userDTO.getUserId())
      .orElseThrow(() -> new RuntimeException("User not found"));

    user.setUsername(userDTO.getUsername());
    user.setRole(userDTO.getRole());

    userRepository.save(user);
  }

  public void deleteUser(Integer userId) {
    userRepository.deleteByUserId(userId);
  }

  public int countAllUsers() {
    return (int) userRepository.count();
  }

  public int countByStatus(String status) {
    return userRepository.countByStatus(status);
  }

}
