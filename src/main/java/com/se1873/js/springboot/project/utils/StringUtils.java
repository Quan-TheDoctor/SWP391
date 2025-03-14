package com.se1873.js.springboot.project.utils;

import org.springframework.stereotype.Component;

@Component
public class StringUtils {
  public static String getUserInitial(String username) {
    if (username == null || username.isEmpty()) {
      return "";
    }
    return String.valueOf(username.toUpperCase().charAt(0));
  }

  public static String formatFullName(String firstName, String lastName) {
    return String.format("%s %s",
      firstName != null ? firstName : "",
      lastName != null ? lastName : "");
  }
}
