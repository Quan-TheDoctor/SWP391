package com.se1873.js.springboot.project.service.user.command;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.User;

public interface UserCommandService {
  void createUserAccount(Employee employee);
  void saveUser(User user);
}
