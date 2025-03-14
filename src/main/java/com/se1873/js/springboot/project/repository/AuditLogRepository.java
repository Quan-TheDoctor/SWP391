package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  int countByActionTypeAndCreatedAtBetween(String actionType, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

  Page<AuditLog> findByUserUsernameContainingIgnoreCaseOrActionInfoContainingIgnoreCase(String userUsername, String actionInfo, Pageable pageable);

  Page<AuditLog> findByActionType(String actionType, Pageable pageable);

  Page<AuditLog> findByActionLevel(String actionLevel, Pageable pageable);

  Page<AuditLog> findByCreatedAtBetween(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore, Pageable pageable);
  @Modifying
  @Query(value = "INSERT INTO audit_logs (user_id, action_info, action_type, action_level, created_at) VALUES (:userId, :actionInfo, :actionType, :actionLevel, CURRENT_TIMESTAMP)", nativeQuery = true)
  @Transactional
  void createAuditLogWithUserIds(@Param("userId") Integer userId,
                                 @Param("actionInfo") String actionInfo,
                                 @Param("actionType") String actionType,
                                 @Param("actionLevel") String actionLevel);
}
