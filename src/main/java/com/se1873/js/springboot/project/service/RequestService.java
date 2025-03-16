package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.LeaveDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Lấy danh sách requests đi kèm pagination
     *
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
            case "month" -> {
                String[] date = value.split("-");
                Integer year = Integer.parseInt(date[1]);
                Integer month = Integer.parseInt(date[0]);
                yield requestRepository.findByDate(pageable, year, month).map(this::convertRequestToDTO);
            }
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
        List<Leave> leaves = leaveRepository.findLeaveByEmployee_EmployeeIdAndStartDateAndEndDate(employee.getEmployeeId(),
                                                                                            requestDTO.getLeaveDTO().getStartDate(),
                                                                                            requestDTO.getLeaveDTO().getEndDate());
        Leave leave = leaves.getFirst();
        if(requestDTO.getRequestStatus().equals("approve")){
            leave.setStatus("approve");
            leave.setLeaveAllowedDay(requestDTO.getLeaveDTO().getLeaveAllowedDay());
            leaveRepository.save(leave);

            for (int i = 0; i < leave.getTotalDays(); i++) {
                Attendance attendance = Attendance.builder()
                        .date(leave.getStartDate().plusDays(i))
                        .checkIn(LocalTime.of(0, 0, 0))
                        .checkOut(LocalTime.of(0, 0, 0))
                        .overtimeHours(0.0)
                        .status("Nghỉ")
                        .note(leave.getReason())
                        .employee(employee)
                        .build();

                attendanceRepository.save(attendance);
            }
        }else{
            leave.setStatus("deny");
            leaveRepository.save(leave);
        }

    }
    public void saveRequestForLeave(RequestDTO requestDTO, User user, User approval) {
        if (user == null) {
            throw new RuntimeException("user ko tồn tại");
        }
        int employeeId = user.getEmployee().getEmployeeId();
        Employee employee = employeeRepository.getEmployeeByEmployeeId(employeeId);

        Leave leave = Leave.builder()
                .leaveType("Đơn xin nghỉ")
                .startDate(requestDTO.getLeaveDTO().getStartDate())
                .endDate(requestDTO.getLeaveDTO().getEndDate())
                .totalDays(requestDTO.getLeaveDTO().getTotalDays())
                .status("pending")
                .leaveAllowedDay(requestDTO.getLeaveDTO().getLeaveAllowedDay())
                .reason(requestDTO.getLeaveDTO().getReason())
                .createdAt(LocalDateTime.now())
                .employee(employee)
                .leavePolicyId(requestDTO.getLeaveDTO().getLeavePolicyId())
                .build();
        leaveRepository.save(leave);
                log.info(String.valueOf(requestDTO.getLeaveDTO().getLeavePolicyId()));
        Integer leaveId = leave.getLeaveId();

        Request request = Request.builder()
                .requesterId(user.getUserId())
                .requestType("Đơn xin nghỉ")
                .reason(requestDTO.getLeaveDTO().getReason())
                .startDate(requestDTO.getLeaveDTO().getEndDate())
                .endDate(requestDTO.getLeaveDTO().getEndDate())
                .totalDays(requestDTO.getLeaveDTO().getTotalDays())
                .note(requestDTO.getNote())
                .status("pending")
                .requestIdList(leaveId.toString())
                .createdAt(LocalDateTime.now())
                .user(user)
                .approval(approval)
                .build();
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
        if ("Đơn xin nghỉ".equals(type)) {
            String status = "approve".equals(requestDTO.getRequestStatus()) ? "approve" : "deny";
            Request request = requestRepository.findRequestByRequestId(requestDTO.getRequestId());
            request.setApproval(approval);
            request.setStatus(status);
            requestRepository.save(request);
        } else if ("Hạch toán lương".equals(type)) {
            for (var payrollId : requestDTO.getPayrollIds()) {
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
        if (request == null) return null;

        LeaveDTO leaveDTO = null;
        List<Integer> idList = Arrays.stream(request.getRequestIdList().replace("[", "").replace("]", "").replace(" ", "").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        if ("Đơn xin nghỉ".equals(request.getRequestType()) && !idList.isEmpty()) {
            Leave leave = leaveRepository.findById(Long.valueOf(idList.get(0))).orElse(null);

            if (leave != null) {
                leaveDTO = LeaveDTO.builder()
                        .leaveId(leave.getLeaveId())
                        .startDate(leave.getStartDate())
                        .endDate(leave.getEndDate())
                        .totalDays(leave.getTotalDays())
                        .reason(leave.getReason())
                        .leavePolicyId(leave.getLeavePolicyId())
                        .leaveAllowedDay(leave.getLeaveAllowedDay())
                        .build();
            }
        }

        return RequestDTO.builder()
                .requestId(request.getRequestId())
                .approvalName(request.getApproval() != null ? request.getApproval().getUsername() : null)
                .requestType(request.getRequestType())
                .requesterId(request.getUser() != null ? request.getUser().getUserId() : null)
                .requesterName(request.getUser() != null ? request.getUser().getUsername() : null)
                .requestDate(request.getCreatedAt() != null ? request.getCreatedAt().toLocalDate() : null)
                .requestStatus(request.getStatus())
                .note(request.getNote())
                .leaveDTO(leaveDTO)
                .payrollIds(idList)
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
