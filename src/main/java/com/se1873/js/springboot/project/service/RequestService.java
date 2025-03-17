package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.LeaveDTO;
import com.se1873.js.springboot.project.dto.RequestCreationRequestDTO;
import com.se1873.js.springboot.project.dto.RequestCreationResponseDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
  private final DepartmentRepository departmentRepository;
  private final EmploymentHistoryRepository employmentHistoryRepository;

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

    for(int i = 0; i < leave.getTotalDays(); i++) {
      Attendance attendance = Attendance.builder()
              .date(leave.getStartDate().plusDays(i))
              .checkIn(null)
              .checkOut(null)
              .overtimeHours(0.0)
              .status("Nghỉ")
              .note(leave.getReason())
              .employee(employee)
              .build();

      attendanceRepository.save(attendance);
    }

    Optional<User> approval = userRepository.findUserByUsername("admin");
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
    request.setApproval(approval.get());
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

    String requestIdList = request.getRequestIdList();
    List<Integer> integerList = new ArrayList<>();
    if (requestIdList != null) {
      String[] ids = requestIdList.replace("[", "").replace("]", "").replace(" ", "").split(",");
      for (String p : ids) {
        try {
          integerList.add(Integer.parseInt(p));
        } catch (NumberFormatException e) {
          System.err.println("Lỗi khi chuyển đổi requestId: " + p);
        }
      }
    }

    return RequestDTO.builder()
            .requestId(request.getRequestId())
            .approvalName(request.getApproval() != null ? request.getApproval().getUsername() : "Unknown")
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

  public RequestCreationResponseDTO createRequest(RequestCreationRequestDTO creationRequest) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();

    Department department = departmentRepository.findById(creationRequest.getDepartmentId())
            .orElseThrow(() -> new RuntimeException("Department not found"));

    User userCreate = userRepository.findUserByUsername(name)
            .orElseThrow(() -> new RuntimeException("User not found"));

    User user = userRepository.findUserByUsername(creationRequest.getEmployeeName())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    Employee employee = user.getEmployee();

    Double currentSalary = employee.getSalaryRecords().stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparing(SalaryRecord::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))  // So sánh các giá trị không null
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
              .baseSalary(newSalary)  // Mức lương cơ bản
              .month(LocalDate.now().getMonthValue())  // Tháng hiện tại
              .year(LocalDate.now().getYear())  // Năm hiện tại
              .totalAllowance(0.0)  // Tổng trợ cấp (có thể thay bằng giá trị hợp lý nếu có)
              .overtimePay(0.0)  // Tiền làm thêm giờ (có thể thay bằng giá trị hợp lý nếu có)
              .deductions(0.0)  // Khấu trừ (có thể thay bằng giá trị hợp lý nếu có)
              .insuranceDeduction(0.0)  // Khấu trừ bảo hiểm
              .taxAmount(0.0)  // Thuế
              .netSalary(newSalary)  // Lương thực nhận, bạn có thể tính theo các trường trên
              .paymentStatus("PENDING")  // Trạng thái thanh toán (có thể thay đổi tùy nhu cầu)
              .createdAt(LocalDateTime.now())  // Thời gian tạo
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
      employmentHistoryRepository.save(history);

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
      employmentHistoryRepository.save(history);
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

  public List<RequestCreationResponseDTO> getAllRequests() {
    List<Request> requests = requestRepository.findAll();

    return requests.stream()
            .map(request -> {
              Employee employee = request.getUser().getEmployee();

              if (employee == null || employee.getEmploymentHistories() == null || employee.getEmploymentHistories().isEmpty()) {
                return null; // Hoặc trả về giá trị mặc định
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
                    (existing, replacement) -> replacement)) // Nếu có bản ghi trùng employeeCode, giữ lại bản ghi mới nhất
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

}
