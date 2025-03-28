package com.se1873.js.springboot.project.service.role;

import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.service.role.command.RoleCommandService;
import com.se1873.js.springboot.project.service.role.query.RoleQueryService;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoleService {

  private final RoleQueryService roleQueryService;
  private final RoleCommandService roleCommandService;

  public RoleService(RoleQueryService roleQueryService, RoleCommandService roleCommandService) {
    this.roleQueryService = roleQueryService;
    this.roleCommandService = roleCommandService;
  }

  @PostConstruct
  public void init() {
    findAll();
  }

  public Map<String, Role> findAll() {
    List<Role> roles = roleQueryService.getAll();
    return roles.stream()
      .collect(Collectors.toMap(
        Role::getName,
        Function.identity(),
        (oldVal, newVal) -> oldVal,
        LinkedHashMap::new
      ));
  }

  @CacheEvict(value = "allRoles", allEntries = true)
  public void updateRole(Role role) {
    roleCommandService.updateRole(role);
  }

  @CacheEvict(value = "allRoles", allEntries = true)
  public void createRole(Role role) {
    roleCommandService.createRole(role);
  }

  @CacheEvict(value = "allRoles", allEntries = true)
  public void deleteRole(Long id) {
    roleCommandService.deleteRole(id);
  }

  public Role findByName(String name) {
      return findAll().get(name);
  }
}
