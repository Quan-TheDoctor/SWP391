package com.se1873.js.springboot.management.controller;

import com.se1873.js.springboot.management.model.Example;
import com.se1873.js.springboot.management.repository.ExampleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "https://localhost:8081/api/")
@RestController
@RequestMapping("/api")
public class ExampleController {
  private final ExampleRepository exampleRepository;

  ExampleController(ExampleRepository exampleRepository) {
    this.exampleRepository = exampleRepository;
  }

  @GetMapping("/example")
  public ResponseEntity<List<Example>> getAllExamples(@RequestParam(required = false) String title) {
//    try {
//      List<Example> examples = new ArrayList<>();
//      if (title == null) {
//        examples = exampleRepository.findAll();
//      } else {
//        exampleRepository.findAllByName(title);
//      }
//      if (examples.isEmpty()) {
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//      }
//      return new ResponseEntity<>(examples, HttpStatus.OK);
//    } catch (Exception e) {
//      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
    return (ResponseEntity<List<Example>>) exampleRepository.findAll().stream().map(ResponseEntity::ok).toList();
  }

  @GetMapping("/example/{id}")
  public ResponseEntity<Example> getExampleById(@PathVariable Long id) {
//    Optional<Example> example = exampleRepository.findById(id);
//    if(example.isPresent()) {
//    return new ResponseEntity<>(example.get(), HttpStatus.OK);
//    } else {
//      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }

    return exampleRepository.findById(id).map(ResponseEntity::ok).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping("/example")
  public ResponseEntity<Example> createExample(@RequestBody Example example) {
//    try {
//      Example savedExample = exampleRepository.save(new Example(example.getId(), example.getName()));
//      return new ResponseEntity<>(savedExample, HttpStatus.CREATED);
//    } catch (Exception e) {
//      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
    return new ResponseEntity<>(exampleRepository.save(new Example(example.getId(), example.getName())), HttpStatus.CREATED);
  }

  @PutMapping("/example/{id}")
  public ResponseEntity<Example> updateExample(@PathVariable Long id, @RequestBody Example example) {
    Optional<Example> existingExample = exampleRepository.findById(id);
    if (existingExample.isPresent()) {
      Example updateExample = existingExample.get();
      updateExample.setId(example.getId());
      updateExample.setName(example.getName());
      return new ResponseEntity<>(exampleRepository.save(updateExample), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/example/{id}")
  public ResponseEntity<Example> deleteExample(@PathVariable Long id) {
//    try {
//      exampleRepository.deleteById(id);
//      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    } catch (Exception e) {
//      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
    return exampleRepository.findById(id).map(ResponseEntity::ok).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }


}
