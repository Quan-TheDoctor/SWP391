package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "role_permissions")
@Data
public class RolePermission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_permission_id")
  private int rolePermissionId;

  @Column(name = "role_id")
  private int roleId;

  @Column(name = "permission_id")
  private int permissionId;
}
