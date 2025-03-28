package com.se1873.js.springboot.project.service.role.command;

import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {
  private final RoleRepository roleRepository;

  public RoleCommandServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public void updateRole(Role role) {
    roleRepository.save(role);
  }

  @Override
  public void createRole(Role role) {
    roleRepository.save(role);
  }

  @Override
  public void deleteRole(Long id) {
    roleRepository.deleteById(Math.toIntExact(id));
  }
}
