package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findByUserId(Integer userId);
  Optional<User> findUserByUsername(String userName);
}
