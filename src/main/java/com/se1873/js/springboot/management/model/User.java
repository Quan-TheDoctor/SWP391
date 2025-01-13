package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private int userId;

  @Column(name = "role_permission_id")
  private String rolePermissionId;

  @Column(name = "username")
  private String username;

  @Column(name = "passwordHash")
  private String passwordHash;

  @Column(name = "email")
  private String email;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
