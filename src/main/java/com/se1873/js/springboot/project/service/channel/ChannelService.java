package com.se1873.js.springboot.project.service.channel;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;
import com.se1873.js.springboot.project.mapper.ChannelDTOMapper;
import com.se1873.js.springboot.project.service.channel.query.ChannelQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelService {
  private final ChannelQueryService channelQueryService;
  private final ChannelDTOMapper channelDTOMapper;

  public ChannelService(ChannelQueryService channelQueryService,
                        ChannelDTOMapper channelDTOMapper) {
    this.channelQueryService = channelQueryService;
    this.channelDTOMapper = channelDTOMapper;
  }

  public List<ChannelDTO> getAllChannels() {
    return channelQueryService.getAllChannels().stream().map(channelDTOMapper::toDTO).collect(Collectors.toList());
  }
  public Channel getChannelByChannelId(Integer channelId) {
    return channelQueryService.getChannelByChannelId(channelId);
  }

  public Channel getChannelByChannelName(String channelName) {
    return channelQueryService.getChannelByChannelName(channelName);
  }

  public ChannelDTO createChannel(ChannelDTO channelDTO) {
    Channel channel = Channel.builder()
        .channelName(channelDTO.getChannelName())
        .createdAt(LocalDateTime.now())
        .build();
    
    return channelDTOMapper.toDTO(channelQueryService.saveChannel(channel));
  }
}
