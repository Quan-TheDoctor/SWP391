package com.se1873.js.springboot.project.service.role.command;

import com.se1873.js.springboot.project.entity.Role;

public interface RoleCommandService {
  void updateRole(Role role);
  void createRole(Role role);
  void deleteRole(Long id);
}
