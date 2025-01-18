package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employee_payrolls")
public class EmployeePayroll {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_payroll_id")
  private int payrollId;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "total_amount")
  private double totalAmount;

  @Column(name = "overtime_amount")
  private double overtimeAmount;

  @Column(name = "leave_deduction_amount")
  private double leaveDeductionAmount;

  @Column(name = "bonus_amount")
  private double bonusAmount;

  @Column(name = "final_salary")
  private double finalSalary;

  @Column(name = "payroll_date", columnDefinition = "DATE")
  private String payrollDate;

  @Column(name = "is_paid", columnDefinition = "BIT")
  private boolean isPaid;

  @Column(name = "is_deleted", columnDefinition = "BIT")
  private boolean isDeleted = Boolean.FALSE;
}
