package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "financial_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialPolicy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "financial_policy_id")
  private Integer financialPolicyId;

  @Column(name = "financial_policy_type")
  private String financialPolicyType;

  @Column(name = "financial_policy_name")
  private String financialPolicyName;

  @Column(name = "financial_policy_amount")
  private Double financialPolicyAmount;
}
