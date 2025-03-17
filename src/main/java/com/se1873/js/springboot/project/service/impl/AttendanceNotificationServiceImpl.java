package com.se1873.js.springboot.project.service.impl;

import com.se1873.js.springboot.project.service.AttendanceNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceNotificationServiceImpl implements AttendanceNotificationService {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void sendWSMessage(Integer employeeId, String result, String type) {
    Map<String, Object> notification = new HashMap<>();
    notification.put("employeeId", employeeId);
    notification.put("result", result);
    notification.put("type", type);
    notification.put("timestamp", new Date());

    messagingTemplate.convertAndSend("/topic/attendance", notification);
  }
}
