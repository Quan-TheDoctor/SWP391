package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
  Role getRoleByName(String name);
}