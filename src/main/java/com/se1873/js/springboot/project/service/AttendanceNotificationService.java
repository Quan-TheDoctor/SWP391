package com.se1873.js.springboot.project.service;

public interface AttendanceNotificationService {
  void sendWSMessage(Integer employeeId, String message, String type);
}
