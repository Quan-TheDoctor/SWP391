package com.se1873.js.springboot.management.model;

import com.se1873.js.springboot.management.enums.UserTypeENUM;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private int userId;

  @Column(name = "username")
  private String username;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "user_type")
  private UserTypeENUM userType;

  @Column(name = "user_email")
  private String userEmail;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<AuditLog> auditLogs;
}
