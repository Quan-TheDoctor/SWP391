package com.se1873.js.springboot.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.se1873.js.springboot.project")
@EnableJpaRepositories(basePackages = "com.se1873.js.springboot.project.repository")
@EnableCaching
public class ProjectApplication {

  public static void main(String[] args) throws IOException {
    SpringApplication.run(ProjectApplication.class, args);

  }

}
