package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.ChannelDTO;
import com.se1873.js.springboot.project.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

  Channel getChannelByChannelId(Integer channelId);
  Channel getChannelByChannelName(String channelName);
}
