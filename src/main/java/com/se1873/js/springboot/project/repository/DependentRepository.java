package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {
}
