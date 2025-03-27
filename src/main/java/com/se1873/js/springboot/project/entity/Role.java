package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(unique = true, nullable = false, name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "employee_management_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel employeePermission;

    @Column(name = "user_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel userPermission;

    @Column(name = "attendance_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel attendancePermission;

    @Column(name = "payroll_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel payrollPermission;

    @Column(name = "request_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel requestPermission;

    @Column(name = "recruitment_management_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel recruitmentPermission;

    @Column(name = "system_settings_permission")
    @Enumerated(EnumType.STRING)
    private PermissionLevel systemPermission;
}

