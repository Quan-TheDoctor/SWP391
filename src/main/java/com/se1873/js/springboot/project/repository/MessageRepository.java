package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.MessageDTO;
import com.se1873.js.springboot.project.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> getMessagesByChannel_ChannelId(Integer channelChannelId, Sort sort);
}
