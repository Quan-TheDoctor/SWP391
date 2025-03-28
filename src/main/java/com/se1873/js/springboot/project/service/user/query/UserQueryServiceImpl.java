package com.se1873.js.springboot.project.service.user.query;

import com.se1873.js.springboot.project.dto.UserDTO;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserQueryServiceImpl implements UserQueryService {
  private final UserRepository userRepository;

  public UserQueryServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Integer countAllUsers() {
    return (int) userRepository.count();
  }

  @Override
  public Integer countByStatus(String status) {
    return userRepository.countByStatus(status);
  }

  @Override
  public Page<User> findByStatus(String status, Pageable pageable) {
    return userRepository.findByStatus(status, pageable);
  }

  @Override
  public Page<User> findByRole(String role, Pageable pageable) {
    log.error(userRepository.findByRole(role, pageable).getContent().toString());
    return userRepository.findByRole(role, pageable);
  }

  @Override
  public Page<User> search(String query, Pageable pageable) {
    return userRepository.findByUsernameContainingIgnoreCase(query, pageable);
  }

  @Override
  public Optional<User> findUserByUsername(String username) {
    return userRepository.findUserByUsername(username);
  }

  @Override
  public User findUserByUserId(Integer userId) {
    return userRepository.findUserByUserId(userId);
  }

  @Override
  public Page<User> getAll(Pageable pageable) {
    return userRepository.findAll(pageable);
  }
}
