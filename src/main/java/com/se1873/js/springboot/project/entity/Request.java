package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
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

  @Column(name = "requester_id")
  private Integer requesterId;

  @Column(name = "request_type")
  private String requestType;

  @Column(name = "reason")
  private String reason;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "status")
  private String status;

  @ManyToOne
  @JoinColumn(name = "approval_id")
  private User approval;

  @Column(name = "is_process")
  private Boolean isProcess;

  @Column(name = "request_id_list")
  private String requestIdList;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "total_days")
  private Integer totalDays;

  @Column(name = "note")
  private String note;

  @Column(name = "priority")
  private String priority;
}
