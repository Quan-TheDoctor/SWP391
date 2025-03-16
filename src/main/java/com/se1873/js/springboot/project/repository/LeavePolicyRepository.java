package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface LeavePolicyRepository extends JpaRepository<LeavePolicy,Integer> {
    Optional<LeavePolicy> findLeavePoliciesByLeavePolicyName(String name);

    LeavePolicy findLeavePolicyByLeavePolicyId(Integer id);
}
