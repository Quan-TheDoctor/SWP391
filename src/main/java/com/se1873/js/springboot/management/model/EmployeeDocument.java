package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_documents")
@Data
public class EmployeeDocument {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "employee_document_id")
  private int employeeDocumentId;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column(name = "document_type")
  private String documentType;

  @Column(name = "document_body")
  private String documentBody;

  @Column(name = "document_expiry_date")
  private String documentExpiryDate;

  @Column(name = "is_deleted")
  private boolean isDeleted = Boolean.FALSE;
}
