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

  public void saveLog(AuditLogDTO auditLogDTO) {
    auditLogRepository.save(auditLogDTOMapper.toEntity(auditLogDTO));
  }
}
