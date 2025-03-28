package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ReponseRepo extends JpaRepository<response, Integer> {
    Optional<response> findByRequestId(int requestId);
}
