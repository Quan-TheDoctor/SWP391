package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.FinancialPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialPolicyRepository extends JpaRepository<FinancialPolicy, Long> {

    @Query("SELECT f.financialPolicyAmount FROM FinancialPolicy f WHERE f.financialPolicyId = ?1")
    public double getFinancialPolicyAmount(Integer id);

}
