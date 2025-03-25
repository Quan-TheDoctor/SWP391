package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.service.channel.ChannelService;
import com.se1873.js.springboot.project.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat/")
public class ChatRoomAPI {
  private final MessageService messageService;
  private final ChannelService channelService;

  @GetMapping("/getAllChannels")
  public List<ChannelDTO> getAllChannels() {
    List<ChannelDTO> channels = channelService.getAllChannels();
    return channels;
  }

  @PostMapping("/createChannel")
  public ChannelDTO createChannel(@RequestBody ChannelDTO channelDTO) {
    return channelService.createChannel(channelDTO);
  }

  @GetMapping("/channels/{channelId}/messages")
  public List<MessageDTO> getChannelMessages(
    @PathVariable Integer channelId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    return messageService.getChannelMessages(channelId);
  }

}
