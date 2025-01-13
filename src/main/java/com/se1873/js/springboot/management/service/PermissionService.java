package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.model.Permission;
import com.se1873.js.springboot.management.repository.PermissionRepository;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
  private final PermissionRepository permissionRepository;
  public PermissionService(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public Permission getByPermissionId(int permissionId) {
    return permissionRepository.getByPermissionId(permissionId).orElseThrow(() -> new RuntimeException("Permission not found"));
  }
}
