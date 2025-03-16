package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.Notification;
import com.se1873.js.springboot.project.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public void updateStatus(String status,Integer notificationId){
        Notification notification = notificationRepository.getNotificationByNotificationId(notificationId);
        notification.setStatus(status);
        notificationRepository.save(notification);
    }
}
