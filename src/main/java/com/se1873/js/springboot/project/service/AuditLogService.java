package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  public void save(AuditLog auditLog) {
    auditLogRepository.save(auditLog);
  }
}
