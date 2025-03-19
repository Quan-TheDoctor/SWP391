package com.se1873.js.springboot.project.service.channel.query;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;

import java.util.List;

public interface ChannelQueryService {
  List<Channel> getAllChannels();
  List<MessageDTO> getChannelMessages(Integer channelId);
  Channel getChannelByChannelId(Integer channelId);
  Channel getChannelByChannelName(String channelName);
}
