package com.se1873.js.springboot.management;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.se1873.js.springboot.management.repository")
public class ManagementApplication {
  @Value("spring.datasource.url")
  private String dbUrl;

  public static void main(String[] args) {
    SpringApplication.run(ManagementApplication.class, args);
  }

}
