package com.se1873.js.springboot.project.service.role.query;

import com.se1873.js.springboot.project.entity.Role;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface RoleQueryService {
  List<Role> getAll();
  Role getByName(String roleName);
}
