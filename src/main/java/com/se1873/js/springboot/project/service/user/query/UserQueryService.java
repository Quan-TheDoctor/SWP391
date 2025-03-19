package com.se1873.js.springboot.project.service.user.query;

import com.se1873.js.springboot.project.dto.UserDTO;
import com.se1873.js.springboot.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserQueryService {
  User findUserByUserId(Integer userId);
  Page<User> getAll(Pageable pageable);
  Optional<User> findUserByUsername(String username);
  Page<User> search(String query, Pageable pageable);
  Page<User> findByRole(String role, Pageable pageable);
  Page<User> findByStatus(String status, Pageable pageable);
  Integer countAllUsers();
  Integer countByStatus(String status);
}
