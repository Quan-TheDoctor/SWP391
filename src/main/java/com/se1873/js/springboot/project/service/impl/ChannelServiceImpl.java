package com.se1873.js.springboot.project.service.impl;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;
import com.se1873.js.springboot.project.entity.Message;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.ChannelDTOMapper;
import com.se1873.js.springboot.project.mapper.MessageDTOMapper;
import com.se1873.js.springboot.project.repository.ChannelRepository;
import com.se1873.js.springboot.project.repository.MessageRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {
  private final MessageDTOMapper messageDTOMapper;
  private final ChannelDTOMapper channelDTOMapper;
  private final ChannelRepository channelRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  @Override
  public List<ChannelDTO> getAllChannels() {
    return channelRepository.findAll().stream().map(channelDTOMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  public MessageDTO saveMessage(MessageDTO messageDTO) {
    Message message = messageDTOMapper.toEntity(messageDTO);
    Channel channel = channelRepository.getChannelByChannelId(messageDTO.getChannelId());
    User user = userRepository.findUserByUserId(messageDTO.getUserId())
        .orElseThrow(null);

    message.setMessageContent(messageDTO.getMessageContent());
    message.setChannel(channel);
    message.setUser(user);
    message.setCreatedAt(messageDTO.getMessageCreatedAt());
    messageRepository.save(message);
    return messageDTO;
  }

  @Override
  public List<MessageDTO> getChannelMessages(Integer channelId) {
    return messageRepository.getMessagesByChannel_ChannelId(channelId, Sort.by("createdAt").descending())
      .stream()
      .map(messageDTOMapper::toDTO)
      .limit(20)
      .collect(Collectors.toList());
  }


  @Override
  public ChannelDTO getChannelByChannelId(Integer channelId) {
    return channelDTOMapper.toDTO(channelRepository.getChannelByChannelId(channelId));
  }
}
