package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.service.ChannelService;
import com.se1873.js.springboot.project.service.impl.ChannelServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat/")
public class ChatRoomAPI {
  private final ChannelService channelService;

  @GetMapping("/getAllChannels")
  public List<ChannelDTO> getAllChannels() {
    List<ChannelDTO> channels = channelService.getAllChannels();
    return channels;
  }


  @GetMapping("/channels/{channelId}/messages")
  public List<MessageDTO> getChannelMessages(
    @PathVariable Integer channelId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    return channelService.getChannelMessages(channelId);
  }

}
