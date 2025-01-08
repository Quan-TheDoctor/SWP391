package com.se1873.js.springboot.management;

import com.se1873.js.springboot.management.controller.ExampleController;
import com.se1873.js.springboot.management.model.Example;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.se1873.js.springboot.management.repository")
public class ManagementApplication implements CommandLineRunner {
  private final ExampleController exampleController;

  public ManagementApplication(ExampleController exampleController) {
    this.exampleController = exampleController;
  }

  public static void main(String[] args) {
    SpringApplication.run(ManagementApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    exampleController.updateExample(1L, new Example(1, "Quann"));
  }
}
