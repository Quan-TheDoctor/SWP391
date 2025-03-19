package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findRequestsByStatus(String status, Pageable pageable);
    Request findRequestByRequestId(Integer requestId);
    Page<Request> findByRequestType(String type, Pageable pageable);
    @Query("SELECT  r.requestType FROM Request r")
    List<String> findRequestTypes();
    @Query("SELECT r FROM Request r WHERE LOWER(r.user.username) LIKE LOWER(CONCAT('%', :requesterName, '%'))")
    Page<Request> searchRequestsByRequester(@Param("requesterName") String requesterName, Pageable pageable);

    Page<Request> findByStatus(String status, Pageable pageable);
}
