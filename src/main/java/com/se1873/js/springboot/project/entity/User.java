package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

  @ToString.Exclude
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
