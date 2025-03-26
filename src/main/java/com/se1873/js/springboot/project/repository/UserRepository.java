package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUserId(Integer userId);
  Optional<User> findUserByUsername(String userName);
  User findUserByUserId(Integer userId);
  List<User> findUsersByUsername(String username);
  @Modifying
  @Query("UPDATE User u SET u.status = :status WHERE u.userId = :userId")
  @Transactional
  void updateUserStatus(@Param("userId") Integer userId, @Param("status") String status);
  void deleteAllByUsername(String username);
  Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
  Page<User> findByRole(String role, Pageable pageable);
  Page<User> findByStatus(String status, Pageable pageable);
  int countByStatus(String status);

  void deleteByUserId(Integer userId);

  User findUserByRole(String role);

  Optional<User> findUserByEmployee_EmployeeId(Integer employeeEmployeeId);

  @Query("SELECT u FROM User u WHERE u.employee.employeeId = (SELECT d.managerId FROM Department d WHERE d.departmentId = :departmentId)")
  Optional<User> findManagerByDepartmentId(@Param("departmentId") Integer departmentId);
}
