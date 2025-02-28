package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.LeaveDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Leave;
import com.se1873.js.springboot.project.entity.Request;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.repository.RequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final LeaveRepository leaveRepository;

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

    public void save(RequestDTO requestDTO, User user, Employee employee){
        Request request = new Request();
        Leave leave = new Leave();
        leave.setLeaveType("đơn xin nghỉ");
        leave.setStartDate(requestDTO.getLeaveDTO().getStartDate());
        leave.setEndDate(requestDTO.getLeaveDTO().getEndDate());
        leave.setTotalDays(requestDTO.getLeaveDTO().getTotalDays());
        leave.setStatus("pending");
        leave.setReason(requestDTO.getLeaveDTO().getReason());
        leave.setCreatedAt(LocalDateTime.now());
        leave.setEmployee(employee);
        leaveRepository.save(leave);
        Integer leaveId = leave.getLeaveId();

        request.setRequestIdList(String.valueOf(leaveId));
        request.setReason(requestDTO.getLeaveDTO().getReason());
        request.setStartDate(requestDTO.getLeaveDTO().getStartDate());
        request.setEndDate(requestDTO.getLeaveDTO().getEndDate());
        request.setNote(requestDTO.getNote());
        request.setTotalDays(requestDTO.getLeaveDTO().getTotalDays());
        request.setRequestType("phiếu xin nghỉ");
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());
        request.setUser(user);



        requestRepository.save(request);
    }


    private RequestDTO convertRequestToDTO(Request request) {
        return RequestDTO.builder()
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
    public Page<RequestDTO> exportFilteredRequests(String status, String type, Pageable pageable) {
        Page<Request> requests;
        if (!"all".equals(status)) {
            requests = requestRepository.findRequestsByStatus(status, pageable);
        } else if (!"all".equals(type)) {
            requests = requestRepository.findByRequestType(type, pageable);
        } else {
            requests = requestRepository.findAll(pageable);
        }

        List<RequestDTO> filteredRequests = requests.getContent().stream()
                .filter(r -> "all".equals(type) || r.getRequestType().equalsIgnoreCase(type))
                .map(this::convertRequestToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(filteredRequests, pageable, filteredRequests.size());
    }

    public Resource exportToExcel(String statusFilter, String typeFilter) {
        Pageable pageable = PageRequest.of(0, 1000);
        List<Request> requests;

        if (!"all".equals(statusFilter)) {
            requests = requestRepository.findRequestsByStatus(statusFilter, pageable).getContent();
        } else if (!"all".equals(typeFilter)) {
            requests = requestRepository.findByRequestType(typeFilter, pageable).getContent();
        } else {
            requests = requestRepository.findAll(pageable).getContent();
        }

        requests = requests.stream()
                .filter(r -> "all".equals(typeFilter) || r.getRequestType().equalsIgnoreCase(typeFilter))
                .collect(Collectors.toList());

        if (requests.isEmpty()) {
            log.warn("No requests available for export.");
            throw new IllegalStateException("No requests available for export.");
        }


        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Requests");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Requester", "Type", "Status", "Date"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Request req : requests) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(req.getRequestId());
                row.createCell(1).setCellValue(req.getUser().getUsername());
                row.createCell(2).setCellValue(req.getRequestType());
                row.createCell(3).setCellValue(req.getStatus());
                row.createCell(4).setCellValue(req.getCreatedAt().toLocalDate().toString());
            }

            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            throw new RuntimeException("Error exporting to Excel", e);
        }
    }
}
