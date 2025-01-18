package com.se1873.js.springboot.management.model;

import com.se1873.js.springboot.management.enums.EmployeeStatusENUM;
import jakarta.persistence.*;

@Entity
@Table(name = "employee_position_histories")
public class EmployeePosition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "position_history_id")
  private int positionHistoryId;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "department_id")
  private Department department;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "role_id")
  private Role role;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "position_id")
  private Position position;

  @Column(name = "position_history_start_date")
  private String positionHistoryStartDate;

  @Column(name = "position_history_end_date")
  private String positionHistoryEndDate;

  @Column(name = "position_history_status")
  private EmployeeStatusENUM positionHistoryStatus;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
