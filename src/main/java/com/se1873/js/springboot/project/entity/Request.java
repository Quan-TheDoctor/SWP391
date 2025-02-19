package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "request_id")
  private Integer requestId;

  @Column(name = "request_type")
  private String requestType;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "status")
  private String status;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "approval_id")
  private User approval;

  @Column(name = "is_process")
  private Boolean isProcess;

  @Column(name = "request_id_list")
  private String requestIdList;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

}
