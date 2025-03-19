package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Notification;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    List<Notification> getNotificationsByUser_UserId(Integer userId);
    Notification getNotificationByNotificationId(Integer notificationId);
}
