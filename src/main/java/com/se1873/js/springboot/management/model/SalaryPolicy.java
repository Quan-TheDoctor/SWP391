package com.se1873.js.springboot.management.model;

import com.se1873.js.springboot.management.enums.SalaryPolicyTypeENUM;
import jakarta.persistence.*;

@Entity
@Table(name = "salary_policies")
public class SalaryPolicy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "salary_policy_id")
  private int salaryPolicyId;

  @Column(name = "salary_policy_name")
  private String salaryPolicyName;

  @Column(name = "salary_policy_type")
  private SalaryPolicyTypeENUM salaryPolicyTypeENUM;

  @Column(name = "salary_policy_amount")
  private int salaryPolicyAmount;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
