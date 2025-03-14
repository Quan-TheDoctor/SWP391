package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.security.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Integer logId;

  @Column(name = "action_info")
  private String actionInfo;

  @Column(name = "action_type")
  private String actionType;

  @Column(name = "action_level")
  private String actionLevel;

  @ToString.Exclude
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
