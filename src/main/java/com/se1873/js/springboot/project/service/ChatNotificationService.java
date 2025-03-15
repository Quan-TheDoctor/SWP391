package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.MessageDTO;

public interface ChatNotificationService {
  void sendMessage(MessageDTO messageDTO);
}
