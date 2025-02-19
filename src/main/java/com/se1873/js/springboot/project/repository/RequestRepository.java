package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findRequestsByStatus(String status, Pageable pageable);
    Page<Request> findByRequestType(String type, Pageable pageable);
    @Query("SELECT  r.requestType FROM Request r")
    List<String> findRequestTypes();
}
