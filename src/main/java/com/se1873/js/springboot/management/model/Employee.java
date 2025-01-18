package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_id")
  private int employeeId;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Dependant> dependants;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EmployeeDocument> employeeDocuments;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Salary> salaries;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Attendance> attendances;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EmployeePayroll> employeePayrolls;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EmployeePosition> employeePositions;

  @Column(name = "employee_first_name", length = 20, nullable = false)
  private String employeeFirstName;

  @Column(name = "employee_last_name", length = 20, nullable = false)
  private String employeeLastName;

  @Column(name = "employee_email", length = 20)
  private String employeeEmail;

  @Column(name = "employee_phone", length = 20)
  private String employeePhone;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;

  public int getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(int employeeId) {
    this.employeeId = employeeId;
  }

  public List<Dependant> getDependants() {
    return dependants;
  }

  public void setDependants(List<Dependant> dependants) {
    this.dependants = dependants;
  }

  public List<EmployeeDocument> getEmployeeDocuments() {
    return employeeDocuments;
  }

  public void setEmployeeDocuments(List<EmployeeDocument> employeeDocuments) {
    this.employeeDocuments = employeeDocuments;
  }

  public List<Salary> getSalaries() {
    return salaries;
  }

  public void setSalaries(List<Salary> salaries) {
    this.salaries = salaries;
  }

  public List<Attendance> getAttendances() {
    return attendances;
  }

  public void setAttendances(List<Attendance> attendances) {
    this.attendances = attendances;
  }

  public List<EmployeePayroll> getEmployeePayrolls() {
    return employeePayrolls;
  }

  public void setEmployeePayrolls(List<EmployeePayroll> employeePayrolls) {
    this.employeePayrolls = employeePayrolls;
  }

  public List<EmployeePosition> getEmployeePositions() {
    return employeePositions;
  }

  public void setEmployeePositions(List<EmployeePosition> employeePositions) {
    this.employeePositions = employeePositions;
  }

  public String getEmployeeFirstName() {
    return employeeFirstName;
  }

  public void setEmployeeFirstName(String employeeFirstName) {
    this.employeeFirstName = employeeFirstName;
  }

  public String getEmployeeLastName() {
    return employeeLastName;
  }

  public void setEmployeeLastName(String employeeLastName) {
    this.employeeLastName = employeeLastName;
  }

  public String getEmployeeEmail() {
    return employeeEmail;
  }

  public void setEmployeeEmail(String employeeEmail) {
    this.employeeEmail = employeeEmail;
  }

  public String getEmployeePhone() {
    return employeePhone;
  }

  public void setEmployeePhone(String employeePhone) {
    this.employeePhone = employeePhone;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }

  public Employee() {
  }

  public Employee(String employeeFirstName, String employeeLastName, String employeeEmail, String employeePhone) {
    this.employeeFirstName = employeeFirstName;
    this.employeeLastName = employeeLastName;
    this.employeeEmail = employeeEmail;
    this.employeePhone = employeePhone;
  }
}
