package com.se1873.js.springboot.project.dto;

import com.se1873.js.springboot.project.entity.User;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
  private Integer channelId;
  private String channelName;

  private Integer messageId;
  private String messageContent;
  private LocalDateTime messageCreatedAt;

  private Integer userId;
  private String username;
  private String userRole;

}
