package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> getByPermissionId(int permissionId);
}
