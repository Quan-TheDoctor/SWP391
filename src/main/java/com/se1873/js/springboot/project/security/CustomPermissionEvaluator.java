package com.se1873.js.springboot.project.security;

import com.se1873.js.springboot.project.entity.PermissionLevel;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.RoleRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        
        User user = userRepository.findUserByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        
        String userRole = user.getRole();
        Role role = roleRepository.getRoleByName(userRole);
        if (role == null) {
            log.debug("Role not found in database for role name: {}", userRole);
            return false;
        }

        String module = targetDomainObject.toString();
        String requiredPermission = permission.toString();
        
        boolean hasPermission = checkPermission(role, module, requiredPermission);
        return hasPermission;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType, permission);
    }

    private boolean checkPermission(Role role, String module, String requiredPermission) {
        log.debug("Checking permission for module: {} with required permission: {}", module, requiredPermission);
        
        switch (module.toUpperCase()) {
            case "EMPLOYEE":
                return checkEmployeePermission(role, requiredPermission);
            case "USER":
                return checkUserPermission(role, requiredPermission);
            case "ATTENDANCE":
                return checkAttendancePermission(role, requiredPermission);
            case "PAYROLL":
                return checkPayrollPermission(role, requiredPermission);
            case "REQUEST":
                return checkRequestPermission(role, requiredPermission);
            case "RECRUITMENT":
                return checkRecruitmentPermission(role, requiredPermission);
            case "SYSTEM":
                return checkSystemPermission(role, requiredPermission);
            default:
                return false;
        }
    }

    private boolean checkEmployeePermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getEmployeePermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            log.debug("Current permission is null or NONE");
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }

    private boolean checkUserPermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getUserPermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }

    private boolean checkAttendancePermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getAttendancePermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }

    private boolean checkPayrollPermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getPayrollPermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }

    private boolean checkRequestPermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getRequestPermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }

    private boolean checkRecruitmentPermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getRecruitmentPermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }

    private boolean checkSystemPermission(Role role, String requiredPermission) {
        PermissionLevel currentPermission = role.getSystemPermission();
        if (currentPermission == null || currentPermission == PermissionLevel.NONE) {
            return false;
        }

        switch (requiredPermission.toUpperCase()) {
            case "MANAGE":
                return currentPermission == PermissionLevel.MANAGE;
            case "UPDATE":
                return currentPermission == PermissionLevel.MANAGE || currentPermission == PermissionLevel.UPDATE;
            case "ADD":
                return currentPermission == PermissionLevel.MANAGE || 
                       currentPermission == PermissionLevel.UPDATE || 
                       currentPermission == PermissionLevel.ADD;
            case "VISIBLE":
                return currentPermission != PermissionLevel.NONE;
            default:
                return false;
        }
    }
} 