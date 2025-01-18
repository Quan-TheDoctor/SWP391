package com.se1873.js.springboot.management.model;

import com.se1873.js.springboot.management.enums.DependantRelationshipENUM;
import jakarta.persistence.*;

@Entity
@Table(name = "dependants")
public class Dependant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dependant_id")
  private int id;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "dependant_name", length = 50)
  private String dependantName;

  @Column(name = "dependant_phone", length = 15)
  private String dependantPhone;

  @Column(name = "dependant_address", length = 500)
  private String dependantAddress;

  @Enumerated(EnumType.STRING)
  @Column(name = "dependant_relationship")
  private DependantRelationshipENUM dependantRelationshipENUM;

  @Column(name = "is_deleted", columnDefinition = "BIT")
  private boolean isDeleted = Boolean.FALSE;

  public Dependant() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Employee getEmployee() {
    return employee;
  }

  public void setEmployee(Employee employee) {
    this.employee = employee;
  }

  public String getDependantName() {
    return dependantName;
  }

  public void setDependantName(String dependantName) {
    this.dependantName = dependantName;
  }

  public String getDependantPhone() {
    return dependantPhone;
  }

  public void setDependantPhone(String dependantPhone) {
    this.dependantPhone = dependantPhone;
  }

  public String getDependantAddress() {
    return dependantAddress;
  }

  public void setDependantAddress(String dependantAddress) {
    this.dependantAddress = dependantAddress;
  }

  public DependantRelationshipENUM getDependantRelationshipENUM() {
    return dependantRelationshipENUM;
  }

  public void setDependantRelationshipENUM(DependantRelationshipENUM dependantRelationshipENUM) {
    this.dependantRelationshipENUM = dependantRelationshipENUM;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }
}
