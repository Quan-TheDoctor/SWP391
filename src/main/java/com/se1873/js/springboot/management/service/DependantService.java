package com.se1873.js.springboot.management.service;

import com.se1873.js.springboot.management.model.Dependant;
import com.se1873.js.springboot.management.repository.DependantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DependantService {
  @Autowired
  private DependantRepository dependantRepository;

  public void insertDependant(Dependant dependant) {
    dependantRepository.save(dependant);
  }

  public void deleteAllDependants() {
    dependantRepository.deleteAll();
  }
}
