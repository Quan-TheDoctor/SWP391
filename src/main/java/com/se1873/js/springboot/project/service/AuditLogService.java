package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.mapper.AuditLogDTOMapper;
import com.se1873.js.springboot.project.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogDTOMapper auditLogDTOMapper;

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

  public void saveLog(AuditLogDTO auditLogDTO) {
    auditLogRepository.save(auditLogDTOMapper.toEntity(auditLogDTO));
  }
}
