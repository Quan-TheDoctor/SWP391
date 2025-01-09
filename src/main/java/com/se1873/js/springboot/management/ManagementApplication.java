package com.se1873.js.springboot.management;

import com.se1873.js.springboot.management.controller.ExampleController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.se1873.js.springboot.management.repository")
public class ManagementApplication {
  private final ExampleController exampleController;

  public ManagementApplication(ExampleController exampleController) {
    this.exampleController = exampleController;
  }

  public static void main(String[] args) {
    SpringApplication.run(ManagementApplication.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> {


    };
  }
}
