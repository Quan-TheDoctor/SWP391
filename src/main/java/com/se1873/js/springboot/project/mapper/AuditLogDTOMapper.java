package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import org.mapstruct.*;

import java.util.List;

@Mapper(
  componentModel = "spring",
  uses = {
    UserDTOMapper.class
  }
)
public abstract class AuditLogDTOMapper {

  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "userRole", ignore = true)
  @Mapping(target = "userCreatedAt", ignore = true)

  public abstract AuditLogDTO toDTO(AuditLog auditLog);

  @AfterMapping
  protected void mapNestedFields(
    AuditLog auditLog,
    @MappingTarget AuditLogDTO auditLogDTO
  ) {
    auditLogDTO.setLogId(auditLog.getLogId());
    auditLogDTO.setActionInfo(auditLog.getActionInfo());
    auditLogDTO.setActionType(auditLog.getActionType());
    auditLogDTO.setActionLevel(auditLog.getActionLevel());
    auditLogDTO.setCreatedAt(auditLog.getCreatedAt());

    if (auditLog.getUser() != null) {
      mapUser(auditLog, auditLogDTO);
    }
  }

  private void mapUser(AuditLog auditLog, AuditLogDTO auditLogDTO) {
    auditLogDTO.setUser(auditLog.getUser());
  }

  public abstract List<AuditLogDTO> toDTOList(List<AuditLog> auditLogs);

  public abstract AuditLog toEntity(AuditLogDTO auditLogDTO);
}
