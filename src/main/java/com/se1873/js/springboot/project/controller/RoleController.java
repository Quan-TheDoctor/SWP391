package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.RolePermissionDTO;
import com.se1873.js.springboot.project.entity.PermissionLevel;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/permissions")
    public ResponseEntity<?> updateRolePermissions(@RequestBody RolePermissionDTO dto) {
        try {
            Role role = roleService.findByName(dto.getRoleName());
            if (role == null) {
                log.error("Role not found: {}", dto.getRoleName());
                return ResponseEntity.badRequest().body("Role not found: " + dto.getRoleName());
            }

            role.setEmployeePermission(dto.getEmployeePermission());
            role.setUserPermission(dto.getUserPermission());
            role.setAttendancePermission(dto.getAttendancePermission());
            role.setPayrollPermission(dto.getPayrollPermission());
            role.setRequestPermission(dto.getRequestPermission());
            role.setRecruitmentPermission(dto.getRecruitmentPermission());
            role.setSystemPermission(dto.getSystemPermission());

            roleService.updateRole(role);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating permissions for role: {}", dto.getRoleName(), e);
            return ResponseEntity.internalServerError().body("Error updating permissions: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try {
            if (role.getEmployeePermission() == null) role.setEmployeePermission(PermissionLevel.NONE);
            if (role.getUserPermission() == null) role.setUserPermission(PermissionLevel.NONE);
            if (role.getAttendancePermission() == null) role.setAttendancePermission(PermissionLevel.NONE);
            if (role.getPayrollPermission() == null) role.setPayrollPermission(PermissionLevel.NONE);
            if (role.getRequestPermission() == null) role.setRequestPermission(PermissionLevel.NONE);
            if (role.getRecruitmentPermission() == null) role.setRecruitmentPermission(PermissionLevel.NONE);
            if (role.getSystemPermission() == null) role.setSystemPermission(PermissionLevel.NONE);

            roleService.createRole(role);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error creating role: {}", role.getName(), e);
            return ResponseEntity.internalServerError().body("Error creating role: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting role with id: {}", id, e);
            return ResponseEntity.internalServerError().body("Error deleting role: " + e.getMessage());
        }
    }
} 