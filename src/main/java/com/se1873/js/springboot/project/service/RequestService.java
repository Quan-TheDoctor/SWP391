package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.api.PayrollAPI;
import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.employment_history.EmploymentHistoryService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import com.se1873.js.springboot.project.utils.EmailUtils;
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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.CacheEvict;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * Service xử lý logic cho request từ Controller
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestService {
  private static final String DEFAULT_STATUS = "Pending";
  private static final String LEAVE_REQUEST_TYPE = "Leave Permit";

  private final RequestRepository requestRepository;
  private final LeaveRepository leaveRepository;
  private final UserRepository userRepository;
  private final SalaryRecordRepository salaryRecordRepository;
  private final AttendanceService attendanceService;
  private final AttendanceRepository attendanceRepository;
  private final EmployeeService employeeService;
  private final DepartmentService departmentService;
  private final EmploymentHistoryService employmentHistoryService;
  private final DepartmentRepository departmentRepository;
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final EmployeeRepository employeeRepository;
  private final ReponseRepo responsRepository;
  private final LeavePolicyRepository leavePolicyRepository;
  private final ContractRepository contractRepository;
  private final SpringTemplateEngine templateEngine;
  private final EmailUtils emailUtils;
  private final SalaryRecordService salaryRecordService;

  @Autowired
  @Lazy
  private PayrollAPI payrollAPI;

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
    List<Leave> leaves = leaveRepository.findLeaveByEmployee_EmployeeIdAndStartDateAndEndDate(employee.getEmployeeId(),
            requestDTO.getLeaveDTO().getStartDate(),
            requestDTO.getLeaveDTO().getEndDate());
    Leave leave = leaves.getFirst();
    if(requestDTO.getRequestStatus().equals("Approved")){
      leave.setStatus("Approved");
      leave.setLeaveAllowedDay(requestDTO.getLeaveDTO().getLeaveAllowedDay());
      leaveRepository.save(leave);

      for (int i = 0; i < leave.getTotalDays(); i++) {
        LocalDate currentDate = leave.getStartDate().plusDays(i);
        
        Optional<Attendance> existingAttendance = attendanceRepository.findByDateAndEmployee_EmployeeId(currentDate, employee.getEmployeeId());
        
        if (existingAttendance.isPresent()) {
          Attendance attendance = existingAttendance.get();
          attendance.setStatus("Absent");
          attendance.setNote(leave.getReason());
          attendanceRepository.save(attendance);
        } else {
          Attendance attendance = Attendance.builder()
                  .date(currentDate)
                  .checkIn(LocalTime.of(0, 0, 0))
                  .checkOut(LocalTime.of(0, 0, 0))
                  .overtimeHours(0.0)
                  .status("Absent")
                  .note(leave.getReason())
                  .employee(employee)
                  .build();

          attendanceRepository.save(attendance);
        }
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
            .leaveType("Leave Permit")
            .startDate(requestDTO.getLeaveDTO().getStartDate())
            .endDate(requestDTO.getLeaveDTO().getEndDate())
            .totalDays(requestDTO.getLeaveDTO().getTotalDays())
            .status("Pending")
            .leaveAllowedDay(requestDTO.getLeaveDTO().getLeaveAllowedDay())
            .reason(requestDTO.getLeaveDTO().getReason())
            .createdAt(LocalDateTime.now())
            .employee(employee)
            .leavePolicyId(requestDTO.getLeaveDTO().getLeavePolicyId())
            .build();
    leaveRepository.save(leave);

    Integer leaveId = leave.getLeaveId();

    Request request = Request.builder()
            .requesterId(user.getUserId())
            .requestType("Leave Permit")
            .reason(requestDTO.getLeaveDTO().getReason())
            .startDate(requestDTO.getLeaveDTO().getStartDate())
            .endDate(requestDTO.getLeaveDTO().getEndDate())
            .totalDays(requestDTO.getLeaveDTO().getTotalDays())
            .note(requestDTO.getNote())
            .status("Pending")
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
            .status("Pending")
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

  @Transactional
  @CacheEvict(value = "allPayrolls", allEntries = true)
  public boolean updateStatus(RequestDTO requestDTO, String type) {
    User approval = userRepository.findUserByUsername(requestDTO.getApprovalName())
            .orElse(null);
            
    // Kiểm tra người phê duyệt có phải là người được chỉ định
    Request request = requestRepository.findRequestByRequestId(requestDTO.getRequestId());
    if (request == null || request.getApproval() == null || !request.getApproval().getUserId().equals(approval.getUserId())) {
        return false;
    }

    if("Leave Permit".equals(type)) {
        String status = "Approved".equals(requestDTO.getRequestStatus()) ? "Approved" : "Denied";
        request.setApproval(approval);
        request.setStatus(status);
        requestRepository.save(request);
    } else if("Salary Calculation".equals(type)) {
        for(var payrollId : requestDTO.getPayrollIds()) {
            String status = requestDTO.getRequestStatus().equals("Approved") ? "Paid" : "Cancelled";
            SalaryRecord salaryRecord = salaryRecordRepository.findSalaryRecordBySalaryIdAndIsDeleted(payrollId, false);
            salaryRecord.setPaymentStatus(status);
            salaryRecordRepository.save(salaryRecord);

            PayrollDTO payroll = salaryRecordService.payrollDTO(payrollId);
            String month = String.valueOf(payroll.getSalaryRecordMonth());
            String year = String.valueOf(payroll.getSalaryRecordYear());
            String employeeName = payroll.getEmployeeFirstName() + " " + payroll.getEmployeeLastName();
            String employeeEmail = employeeService.getEmployeeByEmployeeId(payroll.getEmployeeId()).getEmployeePersonalEmail();

            Context context = new Context();
            context.setVariable("payroll", payroll);
            String payslipContent = templateEngine.process("fragments/payroll-slip-fragments", context);

            emailUtils.sendPayslipEmail(employeeEmail, employeeName, month, year, payslipContent);
        }
    } else {
        response responseDetails = responsRepository.findByRequestId(requestDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        String employeeCode = responseDetails.getEmployeeCode();
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        Contract employeeContract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);
        if (employeeContract != null) {
            if ("Approved".equals(requestDTO.getRequestStatus())) {
                employeeContract.setBaseSalary(responseDetails.getNewBaseSalary());
            } else if ("Denied".equals(requestDTO.getRequestStatus())) {
                employeeContract.setBaseSalary(responseDetails.getOldBaseSalary());
            }
            contractRepository.save(employeeContract);
        }
    }
    request.setApproval(approval);
    request.setStatus(requestDTO.getRequestStatus());
    requestRepository.save(request);
    return true;
  }


  private RequestDTO convertRequestToDTO(Request request) {
    if (request == null) return null;

    LeaveDTO leaveDTO = null;
    List<Integer> idList = Arrays.stream(request.getRequestIdList().replace("[", "").replace("]", "").replace(" ", "").split(","))
            .map(Integer::parseInt)
            .collect(Collectors.toList());

    if ("Leave Permit".equals(request.getRequestType()) && !idList.isEmpty()) {
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
  public Integer countPendingRequests() {
    return requestRepository.countByStatus("Pending");
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
  public List<RequestCreationResponseDTO> createRequest(RequestCreationRequestDTO creationRequest) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();

    Department department = departmentRepository.findById(creationRequest.getDepartmentId().longValue())
            .orElseThrow(() -> new RuntimeException("Department not found"));

    User userCreate = userRepository.findUserByUsername(name)
            .orElseThrow(() -> new RuntimeException("User not found"));

    User approver = userRepository.findUserByUserId(1);

    Map<String, Double> employeeIncreases = creationRequest.getEmployeeNamewithSalary();
    if (employeeIncreases == null || employeeIncreases.isEmpty()) {
      throw new RuntimeException("No employees selected or no salary increase provided");
    }

    List<RequestCreationResponseDTO> responseList = new ArrayList<>();

    for (var entry : employeeIncreases.entrySet()) {
      String employeeCode = entry.getKey();
      Double increasePercentage = entry.getValue();

      Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
              .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeCode));

      Contract currentContract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);
      if (currentContract == null) {
        throw new RuntimeException("Employee has no active contract: " + employeeCode);
      }
      
      Double currentSalary = currentContract.getBaseSalary();
      if (currentSalary == null || currentSalary == 0) {
        throw new RuntimeException("Employee has invalid base salary: " + employeeCode);
      }

      Double newSalary = currentSalary + (currentSalary * increasePercentage / 100);

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
              .paymentStatus("Pending")
              .createdAt(LocalDateTime.now())
              .build();

      salaryRecordRepository.save(newSalaryRecord);

      Request request = Request.builder()
              .requesterId(userCreate.getUserId())
              .requestType("Salary Raise")
              .approval(approver)
              .user(userCreate)
              .status("Pending")
              .createdAt(LocalDateTime.now())
              .requestIdList(employee.getEmployeeId().toString())
              .build();

      requestRepository.save(request);

      responseList.add(RequestCreationResponseDTO.builder()
              .requestId(request.getRequestId())
              .fullName(employee.getFirstName() + " " + employee.getLastName())
              .employeeCode(employee.getEmployeeCode())
              .positionName(employee.getEmploymentHistories().stream()
                      .max(Comparator.comparing(EmploymentHistory::getStartDate))
                      .map(EmploymentHistory::getPosition)
                      .map(Position::getPositionName)
                      .orElse(null))
              .oldBaseSalary(currentSalary)
              .newBaseSalary(newSalary)
              .startedAt(employee.getCreatedAt().toLocalDate())
              .build());
    }



    List<response> entityList = responseList.stream()
            .map(response::toEntity)
            .collect(Collectors.toList());

    responsRepository.saveAll(entityList);



    return responseList;
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

  public Page<RequestDTO> multiFilter(String type, String status, String dateRange, String approver, String department, Pageable pageable) {
    try {
      List<Request> allRequests = requestRepository.findAll();
      List<RequestDTO> filteredRequests = new ArrayList<>();

      if (type != null && !type.equals("all")) {
        allRequests = allRequests.stream()
                .filter(r -> r.getRequestType().equals(type))
                .collect(Collectors.toList());
      }

      if (status != null && !status.equals("all")) {
        allRequests = allRequests.stream()
                .filter(r -> r.getStatus().equals(status))
                .collect(Collectors.toList());
      }

      if (dateRange != null && !dateRange.isEmpty()) {
        String[] dates = dateRange.split(" - ");
        if (dates.length == 2) {
          LocalDate startDate = LocalDate.parse(dates[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
          LocalDate endDate = LocalDate.parse(dates[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
          allRequests = allRequests.stream()
                  .filter(r -> {
                    if (r.getCreatedAt() == null) return false;
                    LocalDate requestDate = r.getCreatedAt().toLocalDate();
                    return !requestDate.isBefore(startDate) && !requestDate.isAfter(endDate);
                  })
                  .collect(Collectors.toList());
        }
      }

      if (approver != null && !approver.equals("all")) {
        allRequests = allRequests.stream()
                .filter(r -> r.getApproval() != null && r.getApproval().getUserId().toString().equals(approver))
                .collect(Collectors.toList());
      }

      if (department != null && !department.equals("all")) {
        allRequests = allRequests.stream()
                .filter(r -> {
                  EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(r.getUser().getEmployee().getEmployeeId());
                  return employeeDTO.getDepartmentId().toString().equals(department);
                })
                .collect(Collectors.toList());
      }

      filteredRequests = allRequests.stream()
              .map(this::convertRequestToDTO)
              .collect(Collectors.toList());

      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), filteredRequests.size());
      if (filteredRequests.isEmpty()) {
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
      }

      List<RequestDTO> pageContent = filteredRequests.subList(start, end);

      return new PageImpl<>(pageContent, pageable, filteredRequests.size());
    } catch (Exception e) {
      log.error("Error in multiFilter: {}", e.getMessage(), e);
      throw e;
    }
  }
}