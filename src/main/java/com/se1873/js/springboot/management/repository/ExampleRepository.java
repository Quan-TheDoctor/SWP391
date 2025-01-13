package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.model.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleRepository extends JpaRepository<Example, Long> {
  List<Example> findAllByName(String name);

}
