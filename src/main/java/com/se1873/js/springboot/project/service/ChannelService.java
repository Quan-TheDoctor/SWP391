package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChannelService {
  List<ChannelDTO> getAllChannels();
  ChannelDTO getChannelByChannelId(Integer channelId);
  List<MessageDTO> getChannelMessages(Integer channelId);
  MessageDTO saveMessage(MessageDTO messageDTO);
}
