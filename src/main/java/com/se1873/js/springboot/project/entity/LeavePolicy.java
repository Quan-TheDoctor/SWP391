package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "leave_policies")
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_policy_id")
    private Integer leavePolicyId;

    @Column(name = "leave_policy_name")
    private String leavePolicyName;

    @Column(name = "leave_policy_amount")
    private Integer leavePolicyAmount;
}
