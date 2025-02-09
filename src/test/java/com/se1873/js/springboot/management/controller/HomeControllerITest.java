package com.se1873.js.springboot.management.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HomeControllerITest {
  @Autowired
  private TestRestTemplate template;

  @Test
  void getHello(){
    ResponseEntity<String> response = template.getForEntity("/", String.class);
    assertThat(response.getBody()).isEqualTo("Greetings from Spring Boot!");
  }
}
