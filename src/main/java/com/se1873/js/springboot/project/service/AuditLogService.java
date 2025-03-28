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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogDTOMapper auditLogDTOMapper;
  private final JdbcTemplate jdbcTemplate;

  @Cacheable(value = "allLogs")
  public List<AuditLog> getAllLogs() {
    return auditLogRepository.findAll();
  }

  public Page<AuditLogDTO> getAll(Pageable pageable) {
    List<AuditLog> allLogs = getAllLogs();
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), allLogs.size());
    List<AuditLog> pageContent = allLogs.subList(start, end);
    return new PageImpl<>(pageContent, pageable, allLogs.size()).map(auditLogDTOMapper::toDTO);
  }

  public Map<String, Integer> getQuantity() {
    Map<String, Integer> map = new HashMap<>();
    List<AuditLog> logs = getAllLogs();
    
    Integer totalLogs = logs.size();
    Integer totalErrors = 0;
    Integer totalView = 0;
    Integer totalNavigates = 0;
    Integer totalUpdates = 0;
    Integer totalCreates = 0;

    for(var log : logs) {
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
    List<AuditLog> logs = getAllLogs();
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    return (int) logs.stream()
        .filter(log -> log.getActionType().equals(actionType) &&
                      !log.getCreatedAt().isBefore(startOfDay) &&
                      !log.getCreatedAt().isAfter(endOfDay))
        .count();
  }

  public Page<AuditLogDTO> searchLogs(String query, Pageable pageable) {
    List<AuditLog> allLogs = getAllLogs();
    List<AuditLog> filteredLogs = allLogs.stream()
        .filter(log -> {
          String username = log.getUser() != null ? log.getUser().getUsername() : "";
          String actionInfo = log.getActionInfo() != null ? log.getActionInfo() : "";
          return username.toLowerCase().contains(query.toLowerCase()) ||
                 actionInfo.toLowerCase().contains(query.toLowerCase());
        })
        .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
    List<AuditLog> pageContent = filteredLogs.subList(start, end);
    return new PageImpl<>(pageContent, pageable, filteredLogs.size()).map(auditLogDTOMapper::toDTO);
  }

  public Page<AuditLogDTO> findByActionType(String actionType, Pageable pageable) {
    List<AuditLog> allLogs = getAllLogs();
    List<AuditLog> filteredLogs = allLogs.stream()
        .filter(log -> log.getActionType().equals(actionType))
        .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
    List<AuditLog> pageContent = filteredLogs.subList(start, end);
    return new PageImpl<>(pageContent, pageable, filteredLogs.size()).map(auditLogDTOMapper::toDTO);
  }

  public Page<AuditLogDTO> findByLevel(String level, Pageable pageable) {
    List<AuditLog> allLogs = getAllLogs();
    List<AuditLog> filteredLogs = allLogs.stream()
        .filter(log -> log.getActionLevel().equals(level))
        .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
    List<AuditLog> pageContent = filteredLogs.subList(start, end);
    return new PageImpl<>(pageContent, pageable, filteredLogs.size()).map(auditLogDTOMapper::toDTO);
  }

  public Page<AuditLogDTO> findByDate(LocalDate date, Pageable pageable) {
    List<AuditLog> allLogs = getAllLogs();
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    List<AuditLog> filteredLogs = allLogs.stream()
        .filter(log -> !log.getCreatedAt().isBefore(startOfDay) && !log.getCreatedAt().isAfter(endOfDay))
        .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
    List<AuditLog> pageContent = filteredLogs.subList(start, end);
    return new PageImpl<>(pageContent, pageable, filteredLogs.size()).map(auditLogDTOMapper::toDTO);
  }

  @CacheEvict(value = "allLogs", allEntries = true)
  @Transactional
  public void saveLog(AuditLog auditLog) {
    auditLog.setCreatedAt(LocalDateTime.now());
    auditLogRepository.save(auditLog);
  }

  @CacheEvict(value = "allLogs", allEntries = true)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createLog(Integer userId, String actionInfo, String actionType) {
    jdbcTemplate.update(
      "INSERT INTO audit_logs (user_id, action_info, action_type, created_at) VALUES (?, ?, ?, NOW())",
      userId, actionInfo, actionType
    );
  }

}
