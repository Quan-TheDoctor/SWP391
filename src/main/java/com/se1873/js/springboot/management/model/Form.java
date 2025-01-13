package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "forms")
@Data
public class Form {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "form_id")
  private int formId;

  @Column(name = "employee_id")
  private int employeeId;

  @Column(name = "form_name")
  private String formName;

  @Column(name = "apply_date")
  private String applyDate;

  @Column(name = "form_status")
  private String formStatus;

  @Column(name = "approve_user_id")
  private int approve_user_id;

  @Column(name = "attachment")
  private String attachment;

  @Column(name = "note")
  private String note;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
