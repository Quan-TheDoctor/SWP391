package com.se1873.js.springboot.management.repository;

import com.se1873.js.springboot.management.model.Dependant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DependantRepository extends JpaRepository<Dependant, Long> {

}
