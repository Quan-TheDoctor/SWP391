package com.se1873.js.springboot.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_id")
  private Integer employeeId;

  @Column(name = "employee_code", unique = true, nullable = false)
  private String employeeCode;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @DateTimeFormat(pattern="yyyy-MM-dd")
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "gender")
  private String gender;

  @Column(name = "id_number", unique = true, nullable = false)
  private String idNumber;

  @Column(name = "permanent_address")
  private String permanentAddress;

  @Column(name = "temporary_address")
  private String temporaryAddress;

  @Column(name = "personal_email")
  private String personalEmail;

  @Column(name = "company_email", unique = true)
  private String companyEmail;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "marital_status")
  private String maritalStatus;

  @Column(name = "bank_account")
  private String bankAccount;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "tax_code")
  private String taxCode;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Dependent> dependents = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Contract> contracts = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<EmploymentHistory> employmentHistories = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<TrainingRecord> trainingRecords = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<EmployeeSkill> employeeSkills = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Qualification> qualifications = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<PerformanceReview> performanceReviews = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<SalaryRecord> salaryRecords = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Leave> leaves = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Attendance> attendances = new HashSet<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Allowance> allowances = new HashSet<>();

  @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
  private User user;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee)) return false;
    Employee employee = (Employee) o;
    return employeeId != null && employeeId.equals(employee.employeeId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
