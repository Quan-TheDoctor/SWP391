package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.employment_history.EmploymentHistoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final EmploymentHistoryService employmentHistoryService;
  private final EmployeeRepository employeeRepository;

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
    log.info(String.valueOf(requestDTO.getLeaveDTO().getTotalDays()));
    log.info(String.valueOf(requestDTO.getLeaveDTO().getLeaveAllowedDay()));
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
    if("Đơn xin nghỉ".equals(type)) {

    } else if("Hạch toán lương".equals(type)) {
      for(var payrollId : requestDTO.getPayrollIds()) {
        String status = requestDTO.getRequestStatus().equals("approve") ? "Paid" : "Cancelled";
        SalaryRecord salaryRecord = salaryRecordRepository.findSalaryRecordBySalaryIdAndIsDeleted(payrollId, false);
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
  public List<RequestCreationResponseDTO> getAllRequests() {
    List<Request> requests = requestRepository.findAll();

    return requests.stream()
      .map(request -> {
        Employee employee = request.getUser().getEmployee();

        if (employee == null || employee.getEmploymentHistories() == null || employee.getEmploymentHistories().isEmpty()) {
          return null;
        }

        Position position = employee.getEmploymentHistories().stream()
          .max(Comparator.comparing(EmploymentHistory::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
          .map(EmploymentHistory::getPosition)
          .orElse(null);

        List<SalaryRecord> salaryRecords = employee.getSalaryRecords().stream()
          .sorted(Comparator.comparing(SalaryRecord::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
          .toList();

        log.info("..... {}", salaryRecords);
        Double newBaseSalary = salaryRecords.isEmpty() ? 0.0 : salaryRecords.get(0).getBaseSalary();

        Double oldBaseSalary = salaryRecords.size() > 1 ? salaryRecords.get(1).getBaseSalary() : 0.0;

        return RequestCreationResponseDTO.builder()
          .requestId(request.getRequestId())
          .fullName(employee.getFirstName() + " " + employee.getLastName())
          .employeeCode(employee.getEmployeeCode())
          .positionName(position != null ? position.getPositionName() : "Chưa có vị trí")
          .oldBaseSalary(oldBaseSalary)
          .newBaseSalary(newBaseSalary)
          .startedAt(request.getCreatedAt().toLocalDate())
          .build();
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(
        RequestCreationResponseDTO::getEmployeeCode,
        request -> request,
        (existing, replacement) -> replacement))
      .values()
      .stream()
      .toList();
  }
  public RequestCreationResponseDTO createRequest(RequestCreationRequestDTO creationRequest) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();

    Department department = departmentService.findDepartmentByDepartmentId(creationRequest.getDepartmentId());

    User userCreate = userRepository.findUserByUsername(name)
      .orElseThrow(() -> new RuntimeException("User not found"));

    User user = userRepository.findUserByUsername(creationRequest.getEmployeeName())
      .orElseThrow(() -> new RuntimeException("Employee not found"));

    Employee employee = user.getEmployee();

    Double currentSalary = employee.getSalaryRecords().stream()
      .filter(Objects::nonNull)
      .max(Comparator.comparing(SalaryRecord::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
      .map(SalaryRecord::getBaseSalary)
      .orElse(0.0);

    Double newSalary = 0.0;
    if ("Tăng lương".equals(creationRequest.getRequestType())) {
      Double increasePercentage = creationRequest.getSalaryIncreasePercentage();
      if (increasePercentage == null || increasePercentage <= 0) {
        throw new RuntimeException("Invalid salary increase percentage");
      }

      newSalary = currentSalary + (currentSalary * increasePercentage / 100);

      SalaryRecord newSalaryRecord = SalaryRecord.builder()
        .employee(employee)
        .baseSalary(newSalary)
        .month(LocalDate.now().getMonthValue())
        .year(LocalDate.now().getYear())
        .totalAllowance(0.0)
        .overtimePay(0.0)
        .deductions(0.0)
        .insuranceDeduction(0.0)
        .taxAmount(0.0)
        .netSalary(newSalary)
        .paymentStatus("PENDING")
        .createdAt(LocalDateTime.now())
        .build();

      salaryRecordRepository.save(newSalaryRecord);

      EmploymentHistory history = EmploymentHistory.builder()
        .employee(employee)
        .department(department)
        .position(employee.getEmploymentHistories().stream()
          .max(Comparator.comparing(EmploymentHistory::getStartDate))
          .map(EmploymentHistory::getPosition)
          .orElseThrow(() -> new RuntimeException("No position found")))
        .startDate(LocalDate.now())
        .isCurrent(true)
        .transferReason("Salary increase")
        .createdAt(LocalDateTime.now())
        .build();
      employmentHistoryService.save(history);

    } else if ("Thăng chức".equals(creationRequest.getRequestType())) {
      Position newPosition = department.getPositions().stream()
        .filter(position -> position.getDepartment().equals(department))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No position found"));

      EmploymentHistory history = EmploymentHistory.builder()
        .employee(employee)
        .department(department)
        .position(newPosition)
        .startDate(LocalDate.now())
        .isCurrent(true)
        .transferReason("Promotion")
        .createdAt(LocalDateTime.now())
        .startDate(LocalDate.now())
        .build();
      employmentHistoryService.save(history);
    }

    Request request = Request.builder()
      .requesterId(userCreate.getUserId())
      .requestType(creationRequest.getRequestType())
      .user(user)
      .status("PENDING")
      .createdAt(LocalDateTime.now())
      .build();
    requestRepository.save(request);

    return RequestCreationResponseDTO.builder()
      .requestId(request.getRequestId())
      .fullName(employee.getFirstName() + " " + employee.getLastName())
      .employeeCode(employee.getEmployeeCode())
      .positionName(department.getPositions().stream()
        .filter(position -> position.getDepartment().equals(department))
        .findFirst()
        .map(Position::getPositionName)
        .orElse("No position assigned"))
      .oldBaseSalary(currentSalary)
      .newBaseSalary(newSalary)
      .startedAt(employee.getCreatedAt().toLocalDate())
      .build();
  }

  public com.se1873.js.springboot.project.dto.RequestDetailDTO getDetailRequest(Long requestId) {
    var request = requestRepository.findById(requestId)
      .orElseThrow(() -> new RuntimeException("No request found"));
    String fullName = request.getUser().getEmployee().getFirstName() + " " + request.getUser().getEmployee().getLastName();

    return com.se1873.js.springboot.project.dto.RequestDetailDTO.builder()
      .requestId(requestId)
      .fullName(fullName)
      .employeeCode(request.getUser().getEmployee().getEmployeeCode())
      .positionName(request.getUser().getEmployee().getEmploymentHistories().stream()
        .max(Comparator.comparing(EmploymentHistory::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder())))
        .map(EmploymentHistory::getPosition)
        .map(Position::getPositionName)
        .orElse(null))
      .newsSalary(request.getUser().getEmployee().getSalaryRecords().stream()
        .max(Comparator.comparing(SalaryRecord::getBaseSalary, Comparator.nullsFirst(Comparator.naturalOrder())))
        .map(SalaryRecord::getBaseSalary)
        .orElse(null))
      .createdDate(request.getCreatedAt().toLocalDate())
      .build();
  }
}