package com.se1873.js.springboot.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dependents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dependent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dependent_id")
  private Integer dependentId;

  @Version
  private Long version;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "relationship", nullable = false)
  private String relationship;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "id_number")
  private String idNumber;

  @Column(name = "is_tax_dependent")
  private Boolean isTaxDependent;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Dependent)) return false;
    Dependent dependent = (Dependent) o;
    return dependentId != null && dependentId.equals(dependent.dependentId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
