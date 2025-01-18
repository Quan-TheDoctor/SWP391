package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "roles")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private int roleId;

  @Column(name = "role_name")
  private String roleName;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EmployeePosition> employeePositions;

  @Column(name = "is_deleted", columnDefinition = "BIT")
  private boolean isDeleted = Boolean.FALSE;
}
