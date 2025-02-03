package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.entity.Role;
import com.se1873.js.springboot.management.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public List<Role> findAll() {
    return roleRepository.findAll();
  }
}
