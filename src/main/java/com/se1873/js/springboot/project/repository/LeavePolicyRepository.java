package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Integer> {
    LeavePolicy findLeavePolicyByLeavePolicyId(Integer leavePolicyId);

  LeavePolicy findLeavePolicyByLeavePolicyName(String leavePolicyName);
}
