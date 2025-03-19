package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDTO {
  private Integer logId;
  private String actionInfo;
  private String actionType;
  private String actionLevel;
  private User user;
  private LocalDateTime createdAt;
  private Integer userId;
  private String username;
  private String userRole;
  private LocalDateTime userCreatedAt;
}
