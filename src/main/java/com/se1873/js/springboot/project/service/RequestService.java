package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Request;
import com.se1873.js.springboot.project.repository.RequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    public Page<RequestDTO> getRequests(Pageable pageable) {
        return requestRepository.findAll(pageable).map(this::convertRequestToDTO);
    }
    public Page<RequestDTO> filter(String field, String value, Pageable pageable) {
        switch (field) {
            case "status":
                return requestRepository.findRequestsByStatus(value.toLowerCase(), pageable).map(this::convertRequestToDTO);
            case "type":
                if (value.equalsIgnoreCase("all")) {
                    return requestRepository.findAll(pageable).map(this::convertRequestToDTO);
                }
                return requestRepository.findByRequestType(value, pageable).map(this::convertRequestToDTO);
            default:
                log.warn("Unsupported filter field: {}", field);
                return requestRepository.findAll(pageable).map(this::convertRequestToDTO);
        }
    }
    public List<String> getAllRequestTypes() {
        return requestRepository.findRequestTypes();
    }


    private RequestDTO convertRequestToDTO(Request request) {
        return RequestDTO.builder()
          .requestId(request.getRequestId())
          .requestType(request.getRequestType())
          .requesterId(request.getUser().getUserId())
          .requestDate(request.getCreatedAt().toLocalDate())
          .requestStatus(request.getStatus())
          .requesterName(request.getUser().getUsername())
          .approvalName(request.getApproval().getUsername())
          .build();
    }
    public Page<RequestDTO> searchRequests(String query, Pageable pageable) {
        String requesterName = query;

        Page<Request> requests = requestRepository.searchRequestsByRequester(requesterName, pageable);
        return requests.map(this::convertRequestToDTO);
    }

}
