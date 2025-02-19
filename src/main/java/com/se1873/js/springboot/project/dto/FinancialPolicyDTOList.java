package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.FinancialPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialPolicyDTOList {
    List<FinancialPolicy> financialPolicies;
}
