package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private int roleId;

  @Column(name = "role_name")
  private String roleName;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
