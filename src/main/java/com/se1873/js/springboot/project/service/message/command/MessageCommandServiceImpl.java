package com.se1873.js.springboot.project.service.message.command;

import com.se1873.js.springboot.project.entity.Channel;
import com.se1873.js.springboot.project.entity.Message;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageCommandServiceImpl implements MessageCommandService {
  private final MessageRepository messageRepository;

  public MessageCommandServiceImpl(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public void saveMessage(Message message, Channel channel) {
    messageRepository.save(message);
  }
}
