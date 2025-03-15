package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id")
  private Integer messageId;

  @ToString.Exclude
  @ManyToOne
  @JoinColumn(name = "channel_id")
  private Channel channel;

  @ToString.Exclude
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "message_content")
  private String messageContent;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
