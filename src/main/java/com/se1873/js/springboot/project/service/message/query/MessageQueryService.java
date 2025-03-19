package com.se1873.js.springboot.project.service.message.query;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Message;

import java.util.List;

public interface MessageQueryService {
  List<Message> getChannelMessages(Integer channelId);
}
