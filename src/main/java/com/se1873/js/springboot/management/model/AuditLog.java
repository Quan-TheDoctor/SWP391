package com.se1873.js.springboot.management.model;

import com.se1873.js.springboot.management.enums.SqlActionENUM;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_log_id")
  private int auditLogId;

  @Column(name = "table_name")
  private String tableName;

  @Column(name = "record_id")
  private int recordId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action")
  private SqlActionENUM action;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "changed_timestamp", columnDefinition = "DATETIME")
  private String changedTimestamp;

  @Column(name = "is_deleted", columnDefinition = "BIT")
  private boolean isDeleted = Boolean.FALSE;
}
