package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.entity.Channel;
import org.mapstruct.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
  componentModel = "spring"
)
public abstract class ChannelDTOMapper {
  @Mapping(target = "messageIds", ignore = true)
  public abstract ChannelDTO toDTO(Channel channel);

  @AfterMapping
  protected void mapMessageIds(Channel channel, @MappingTarget ChannelDTO channelDTO) {
    if (channel.getMessages() != null && !channel.getMessages().isEmpty()) {
      List<Integer> messageIds = channel.getMessages().stream()
        .map(message -> message.getMessageId())
        .collect(Collectors.toList());
      channelDTO.setMessageIds(messageIds);
    }
  }

  public abstract List<ChannelDTO> toDTOList(List<Channel> channels);

  @Mapping(target = "messages", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  public abstract Channel toEntity(ChannelDTO channelDTO);

  @AfterMapping
  protected void afterToEntity(ChannelDTO channelDTO, @MappingTarget Channel channel) {

  }
}
