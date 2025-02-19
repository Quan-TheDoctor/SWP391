package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.Request;
import com.se1873.js.springboot.project.repository.RequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    public Page<Request> getRequests(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }
    public Page<Request> filter(String field, String value, Pageable pageable) {
        switch (field) {
            case "status":
                return requestRepository.findRequestsByStatus(value.toLowerCase(), pageable);
            case "type":
                if (value.equalsIgnoreCase("all")) {
                    return requestRepository.findAll(pageable);
                }
                return requestRepository.findByRequestType(value, pageable);
            default:
                log.warn("Unsupported filter field: {}", field);
                return requestRepository.findAll(pageable);
        }
    }
    public List<String> getAllRequestTypes() {
        return requestRepository.findRequestTypes();
    }


}
