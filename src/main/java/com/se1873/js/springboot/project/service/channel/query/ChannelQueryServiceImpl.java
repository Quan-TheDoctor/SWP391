package com.se1873.js.springboot.project.service.channel.query;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;
import com.se1873.js.springboot.project.repository.ChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChannelQueryServiceImpl implements ChannelQueryService {
  private final ChannelRepository channelRepository;

  public ChannelQueryServiceImpl(ChannelRepository channelRepository) {
    this.channelRepository = channelRepository;
  }

  @Override
  public Channel getChannelByChannelId(Integer channelId) {
    return channelRepository.getChannelByChannelId(channelId);
  }

  @Override
  public List<MessageDTO> getChannelMessages(Integer channelId) {
    return List.of();
  }

  @Override
  public List<Channel> getAllChannels() {
    return channelRepository.findAll();
  }

  @Override
  public Channel getChannelByChannelName(String channelName) {
    return channelRepository.getChannelByChannelName(channelName);
  }

  @Override
  public Channel saveChannel(Channel channel) {
    return channelRepository.save(channel);
  }
}
