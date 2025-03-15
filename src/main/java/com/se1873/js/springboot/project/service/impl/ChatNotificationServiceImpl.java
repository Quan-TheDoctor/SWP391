package com.se1873.js.springboot.project.service.impl;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.service.ChatNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatNotificationServiceImpl implements ChatNotificationService {
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void sendMessage(MessageDTO messageDTO) {
    messagingTemplate.convertAndSend("/topic/chat", messageDTO);
  }
}
