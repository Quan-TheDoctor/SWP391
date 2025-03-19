package com.se1873.js.springboot.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "channels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "channel_id")
  private Integer channelId;

  @Column(name = "channel_name")
  private String channelName;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ToString.Exclude
  @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
  private List<Message> messages;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
