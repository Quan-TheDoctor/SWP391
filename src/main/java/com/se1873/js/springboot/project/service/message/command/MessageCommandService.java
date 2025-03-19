package com.se1873.js.springboot.project.service.message.command;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;
import com.se1873.js.springboot.project.entity.Message;

public interface MessageCommandService {
  void saveMessage(Message message,  Channel channel);
}
