package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Message;
import org.mapstruct.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(
  componentModel = "spring",
  uses = {
    UserDTOMapper.class,
    ChannelDTOMapper.class
  }
)
public abstract class MessageDTOMapper {
  @Mapping(target = "channelId", ignore = true)
  @Mapping(target = "channelName", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "userRole", ignore = true)
  @Mapping(target = "messageCreatedAt", ignore = true)
  public abstract MessageDTO toDTO(Message message);

  @AfterMapping
  protected void mapNestedFields(
    Message message,
    @MappingTarget MessageDTO messageDTO
  ) {
    if (message.getChannel() != null) {
      messageDTO.setChannelId(message.getChannel().getChannelId());
      messageDTO.setChannelName(message.getChannel().getChannelName());
    }

    if (message.getUser() != null) {
      messageDTO.setUserId(message.getUser().getUserId());
      messageDTO.setUsername(message.getUser().getUsername());
      messageDTO.setUserRole(message.getUser().getRole());
      messageDTO.setMessageCreatedAt(message.getCreatedAt());
      messageDTO.setMessageContent(message.getMessageContent());
    }
  }

  public abstract List<MessageDTO> toDTOList(List<Message> messages);

  @Mapping(target = "channel", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  public abstract Message toEntity(MessageDTO messageDTO);

  @AfterMapping
  protected void afterToEntity(MessageDTO messageDTO, @MappingTarget Message message) {

  }
}
