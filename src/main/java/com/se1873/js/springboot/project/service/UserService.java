package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;

  public Page<User> getAll(Pageable pageable) {
    return userRepository.findAll(pageable);
  }
}
