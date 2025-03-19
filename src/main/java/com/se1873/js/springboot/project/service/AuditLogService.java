package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.AuditLogDTOMapper;
import com.se1873.js.springboot.project.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogDTOMapper auditLogDTOMapper;
  private final JdbcTemplate jdbcTemplate;

  public Page<AuditLogDTO> getAll(Pageable pageable) {
    return auditLogRepository.findAll(pageable).map(auditLogDTOMapper::toDTO);
  }

  public Map<String, Integer> getQuantity() {
    Map<String, Integer> map = new HashMap<>();
    var logs = auditLogRepository.findAll();
    Integer totalLogs = 0;
    Integer totalErrors = 0;
    Integer totalView = 0;
    Integer totalNavigates = 0;
    Integer totalUpdates = 0;
    Integer totalCreates = 0;

    for(var log : logs) {
      totalLogs++;
      if(log.getActionType().equals("Navigate")) totalNavigates++;
      if(log.getActionType().equals("Error")) totalErrors++;
      if(log.getActionType().equals("Update")) totalUpdates++;
      if(log.getActionType().equals("Create")) totalCreates++;
      if(log.getActionType().equals("View")) totalView++;
    }

    map.put("totalLogs", totalLogs);
    map.put("totalErrors", totalErrors);
    map.put("totalViews", totalView);
    map.put("totalNavigates", totalNavigates);
    map.put("totalUpdates", totalUpdates);
    map.put("totalCreates", totalCreates);
    return map;
  }
  public int countByActionTypeAndDate(String actionType, LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    return auditLogRepository.countByActionTypeAndCreatedAtBetween(
      actionType, startOfDay, endOfDay);
  }

  public Page<AuditLogDTO> searchLogs(String query, Pageable pageable) {
    return auditLogRepository.findByUserUsernameContainingIgnoreCaseOrActionInfoContainingIgnoreCase(query, query, pageable).map(auditLogDTOMapper::toDTO);
  }

  public Page<AuditLogDTO> findByActionType(String actionType, Pageable pageable) {
    return auditLogRepository.findByActionType(actionType, pageable).map(auditLogDTOMapper::toDTO);
  }

  public Page<AuditLogDTO> findByLevel(String level, Pageable pageable) {
    return auditLogRepository.findByActionLevel(level, pageable).map(auditLogDTOMapper::toDTO);
  }
  public Page<AuditLogDTO> findByDate(LocalDate date, Pageable pageable) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    return auditLogRepository.findByCreatedAtBetween(startOfDay, endOfDay, pageable).map(auditLogDTOMapper::toDTO);
  }
  @Transactional
  public void saveLog(AuditLog auditLog) {
    auditLog.setCreatedAt(LocalDateTime.now());
    auditLogRepository.save(auditLog);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createLog(Integer userId, String actionInfo, String actionType) {
    jdbcTemplate.update(
      "INSERT INTO audit_logs (user_id, action_info, action_type, created_at) VALUES (?, ?, ?, NOW())",
      userId, actionInfo, actionType
    );
  }


}
