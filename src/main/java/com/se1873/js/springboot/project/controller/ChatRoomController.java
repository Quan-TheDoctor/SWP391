package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.message.MessageService;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatRoomController {
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final UserService userService;
  private final UserRepository userRepository;
  private final MessageService messageService;

  @MessageMapping("/test.checkSession")
  @SendTo("/topic/test")
  public String checkSession(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
    Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");
    String username = (String) headerAccessor.getSessionAttributes().get("username");

    return "Session info: userId=" + userId + ", username=" + username;
  }

  @MessageMapping("/chat.sendMessage/{channelId}")
  @SendTo("/topic/chat/{channelId}")
  public MessageDTO sendMessage(@DestinationVariable Integer channelId,
                                @Payload MessageDTO messageDTO,
                                Principal principal) {

    if ("System Notification".equals(messageDTO.getUsername())) {
      User adminUser = userRepository.findUserByRole("SYSTEM");

      messageDTO.setUserId(adminUser.getUserId());
      messageDTO.setUsername("System Notification");

      MessageDTO message = messageService.saveMessage(messageDTO);

      return MessageDTO.builder()
        .messageId(message.getMessageId())
        .channelId(channelId)
        .userId(adminUser.getUserId())
        .username("System Notification")
        .messageContent(messageDTO.getMessageContent())
        .messageCreatedAt(LocalDateTime.now())
        .build();
    } else {
      User user = userService.findUserByUsername(principal.getName())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

      messageDTO.setUserId(user.getUserId());
      messageDTO.setUsername(user.getUsername());
      messageDTO.setChannelId(channelId);
      messageDTO.setMessageCreatedAt(LocalDateTime.now());

      messageService.saveMessage(messageDTO);

      return messageDTO;
    }
  }

  @GetMapping
  @PreAuthorize("hasPermission('USER', 'VISIBLE')")
  public String index(Model model,
                      Authentication authentication) {
    String username = ((UserDetails) authentication.getPrincipal()).getUsername();

    User currentUser = userService.findUserByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    model.addAttribute("currentUser", currentUser);
    model.addAttribute("contentFragment", "fragments/chat-room-fragments");
    return "index";
  }
}
