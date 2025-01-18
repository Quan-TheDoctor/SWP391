package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "positions")
@Data
public class Position {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "position_id")
  private int positionId;

  @Column(name = "position_name")
  private String positionName;

  @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EmployeePosition> employeePositions;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
