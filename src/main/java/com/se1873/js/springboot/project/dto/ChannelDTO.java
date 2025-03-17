package com.se1873.js.springboot.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelDTO {
  private Integer channelId;
  private String channelName;
  private String createdAt;

  private List<Integer> messageIds;
}
