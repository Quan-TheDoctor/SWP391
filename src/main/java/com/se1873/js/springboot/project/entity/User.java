package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "username")
  private String username;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "role")
  private String role;

  @Column(name = "status")
  private String status;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ToString.Exclude
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @ToString.Exclude
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<AuditLog> auditLogs;

  @ToString.Exclude
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Message> messages;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
