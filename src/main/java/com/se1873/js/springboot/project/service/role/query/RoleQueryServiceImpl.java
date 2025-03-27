package com.se1873.js.springboot.project.service.role.query;

import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.repository.RoleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleQueryServiceImpl implements RoleQueryService {
  private final RoleRepository roleRepository;

  public RoleQueryServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  @Cacheable(value = "allRoles")
  public List<Role> getAll() {
    return roleRepository.findAll();
  }

  @Override
  public Role getByName(String roleName) {
    return null;
  }
}
