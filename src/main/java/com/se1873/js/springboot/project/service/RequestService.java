package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.LeaveDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.repository.RequestRepository;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic cho request từ Controller
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestService {
  private static final String DEFAULT_STATUS = "pending";
  private static final String LEAVE_REQUEST_TYPE = "Đơn xin nghỉ";

  private final RequestRepository requestRepository;
  private final LeaveRepository leaveRepository;
  private final UserRepository userRepository;
  private final SalaryRecordRepository salaryRecordRepository;

  /**
   * Lấy danh sách requests đi kèm pagination
   * @param pageable Pagination information
   * @return Page of RequestDTOs
   */
  public Page<RequestDTO> getRequests(Pageable pageable) {
    return requestRepository.findAll(pageable).map(this::convertRequestToDTO);
  }

  public Page<RequestDTO> filter(String field, String value, Pageable pageable) {
    return switch (field.toLowerCase()) {
      case "status" -> requestRepository.findByStatus(value, pageable).map(this::convertRequestToDTO);
      case "type" -> filterByType(value, pageable);
      default -> {
        log.warn("Unsupported filter field: {}", field);
        yield getRequests(pageable);
      }
    };
  }
  private Page<RequestDTO> filterByType(String value, Pageable pageable) {
    return "all".equalsIgnoreCase(value)
      ? getRequests(pageable)
      : requestRepository.findByRequestType(value, pageable).map(this::convertRequestToDTO);
  }

  public List<String> getAllRequestTypes() {
    return requestRepository.findRequestTypes();
  }

  public void save(RequestDTO requestDTO, User user, Employee employee) {
    if (user == null) {
      throw new RuntimeException("user ko tồn tại");
    }

    Request request = new Request();
    Leave leave = new Leave();
    leave.setLeaveType("Đơn xin nghỉ");
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
    request.setRequesterId(user.getUserId());
    request.setReason(requestDTO.getLeaveDTO().getReason());
    request.setStartDate(requestDTO.getLeaveDTO().getStartDate());
    request.setEndDate(requestDTO.getLeaveDTO().getEndDate());
    request.setNote(requestDTO.getNote());
    request.setTotalDays(requestDTO.getLeaveDTO().getTotalDays());
    request.setRequestType("Đơn xin nghỉ");
    request.setStatus("pending");
    request.setCreatedAt(LocalDateTime.now());
    request.setUser(user);

    requestRepository.save(request);
  }

  public void saveRequest(RequestDTO requestDTO, User user, User approval) {
    if (user == null) {
      throw new RuntimeException("user ko tồn tại");
    }

    Request request = Request.builder()
      .requesterId(user.getUserId())
      .requestType(requestDTO.getRequestType())
      .status("pending")
      .requestIdList(requestDTO.getPayrollIds().toString())
      .createdAt(LocalDateTime.now())
      .user(user)
      .approval(approval)
      .build();
    requestRepository.save(request);
  }

  public RequestDTO findRequestByRequestId(Integer requestId) {
    return convertRequestToDTO(requestRepository.findRequestByRequestId(requestId));
  }

  public void updateStatus(RequestDTO requestDTO, String type) {
    User approval = userRepository.findUserByUsername(requestDTO.getApprovalName())
      .orElse(null);
    if("Đơn xin nghỉ".equals(type)) {

    } else if("Hạch toán lương".equals(type)) {
      for(var payrollId : requestDTO.getPayrollIds()) {
        String status = requestDTO.getRequestStatus().equals("approve") ? "Paid" : "Cancelled";
        SalaryRecord salaryRecord = salaryRecordRepository.findSalaryRecordBySalaryId(payrollId);
        salaryRecord.setPaymentStatus(status);
        salaryRecordRepository.save(salaryRecord);
      }
    }
    Request request = requestRepository.findRequestByRequestId(requestDTO.getRequestId());
    request.setApproval(approval);
    request.setStatus(requestDTO.getRequestStatus());
    requestRepository.save(request);
  }


  private RequestDTO convertRequestToDTO(Request request) {
    if (request == null) {
      return null;
    }

    LeaveDTO leaveDTO = LeaveDTO.builder()
      .reason(request.getReason())
      .startDate(request.getStartDate())
      .endDate(request.getEndDate())
      .totalDays(request.getTotalDays())
      .build();

    List<Integer> integerList = new ArrayList<>();
    for(var p : request.getRequestIdList().replace("[", "").replace("]","").replace(" ","").split(",")) {
      integerList.add(Integer.parseInt(p));
    }


    return RequestDTO.builder()
      .requestId(request.getRequestId())
      .approvalName(request.getApproval().getUsername())
      .requestType(request.getRequestType())
      .requesterId(request.getUser() != null ? request.getUser().getUserId() : null)
      .requesterName(request.getUser() != null ? request.getUser().getUsername() : null)
      .requestDate(request.getCreatedAt() != null ? request.getCreatedAt().toLocalDate() : null)
      .requestStatus(request.getStatus())
      .note(request.getNote())
      .leaveDTO(leaveDTO)
      .payrollIds(integerList)
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
