package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.PermissionLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDTO {
    private String roleName;
    private PermissionLevel employeePermission;
    private PermissionLevel userPermission;
    private PermissionLevel attendancePermission;
    private PermissionLevel payrollPermission;
    private PermissionLevel requestPermission;
    private PermissionLevel recruitmentPermission;
    private PermissionLevel systemPermission;
} 