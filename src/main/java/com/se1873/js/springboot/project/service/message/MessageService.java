package com.se1873.js.springboot.project.service.message;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Channel;
import com.se1873.js.springboot.project.entity.Message;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.MessageDTOMapper;
import com.se1873.js.springboot.project.service.channel.ChannelService;
import com.se1873.js.springboot.project.service.message.command.MessageCommandService;
import com.se1873.js.springboot.project.service.message.query.MessageQueryService;
import com.se1873.js.springboot.project.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

  private final MessageQueryService messageQueryService;
  private final MessageDTOMapper messageDTOMapper;
  private final ChannelService channelService;
  private final MessageCommandService messageCommandService;
  private final UserService userService;

  public MessageService(MessageQueryService messageQueryService,
                        MessageDTOMapper messageDTOMapper, ChannelService channelService, MessageCommandService messageCommandService, UserService userService) {
    this.messageQueryService = messageQueryService;
    this.messageDTOMapper = messageDTOMapper;
    this.channelService = channelService;
    this.messageCommandService = messageCommandService;
    this.userService = userService;
  }

  public List<MessageDTO> getChannelMessages(Integer channelId) {
    return messageQueryService.getChannelMessages(channelId).stream().map(messageDTOMapper::toDTO).collect(Collectors.toList());
  }

  public MessageDTO saveMessage(MessageDTO messageDTO) {
    Message message = messageDTOMapper.toEntity(messageDTO);
    Channel channel = channelService.getChannelByChannelId(messageDTO.getChannelId());
    User user = userService.findUserByUserId(messageDTO.getUserId());
    message.setMessageContent(messageDTO.getMessageContent());
    message.setChannel(channel);
    message.setUser(user);
    message.setCreatedAt(messageDTO.getMessageCreatedAt());

    messageCommandService.saveMessage(message, channel);
    return messageDTO;
  }
}
