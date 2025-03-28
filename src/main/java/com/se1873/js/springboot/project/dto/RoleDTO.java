package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.PermissionLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private PermissionLevel employeePermission;
    private PermissionLevel departmentPermission;
    private PermissionLevel recruitmentPermission;
    private PermissionLevel systemPermission;
}
