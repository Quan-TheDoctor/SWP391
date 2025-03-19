package com.se1873.js.springboot.project.service.message.query;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Message;
import com.se1873.js.springboot.project.repository.MessageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageQueryServiceImpl implements MessageQueryService {

  private final MessageRepository messageRepository;

  public MessageQueryServiceImpl(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public List<Message> getChannelMessages(Integer channelId) {
    return messageRepository.getMessagesByChannel_ChannelId(channelId, Sort.by("createdAt").descending())
      .stream()
      .limit(20)
      .collect(Collectors.toList());
  }
}
