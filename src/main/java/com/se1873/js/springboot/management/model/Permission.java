package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "permissions")
@Data
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "permission_id")
  private int permissionId;

  @Column(name = "permission_name")
  private String permissionName;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
