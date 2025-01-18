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

  @Enumerated(EnumType.STRING)
  private DependantRelationshipENUM dependantRelationshipENUM;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;

  public Dependant() {}

  public Dependant(int id, Employee employee, String dependantName, String dependantPhone, DependantRelationshipENUM dependantRelationshipENUM, boolean isDeleted) {
    this.id = id;
    this.employee = employee;
    this.dependantName = dependantName;
    this.dependantPhone = dependantPhone;
    this.dependantRelationshipENUM = dependantRelationshipENUM;
    this.isDeleted = isDeleted;
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

  @Override
  public String toString() {
    return "Dependant{" +
      "id=" + id +
      ", employee=" + employee +
      ", dependantName='" + dependantName + '\'' +
      ", dependantPhone='" + dependantPhone + '\'' +
      ", dependantRelationshipENUM=" + dependantRelationshipENUM +
      ", isDeleted=" + isDeleted +
      '}';
  }
}
