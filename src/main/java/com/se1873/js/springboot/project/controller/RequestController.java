package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Request;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.RequestService;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller xử lý requests được gửi từ view bao gồm: view, filter, search, export, update request status,...
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestController {
  private static final Integer DEFAULT_PAGE_SIZE = 5;
  private static final String REQUEST_STATUS_PENDING = "pending";
  private static final String REQUEST_TYPE_LEAVE = "Đơn xin nghỉ";

  private final RequestService requestService;
  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final SalaryRecordRepository salaryRecordRepository;

  private Integer totalRequests = 0;
  private Integer totalPendingRequests = 0;
  private Integer totalFinishedRequests = 0;
  private Set<String> requestTypes = new HashSet<>();

  private Pageable getPageable(Integer page, Integer size) {
    return PageRequest.of(page, size);
  }

  private String populateRequestModel(Model model, Page<RequestDTO> requests, String viewName) {
    model.addAttribute("requests", requests);
    addRequestStatistics(requests, model);
    model.addAttribute("requestTypes", requestService.getAllRequestTypes());
    return viewName;
  }
  private void addRequestStatistics(Page<RequestDTO> requests, Model model) {
    long totalPending = requests.stream()
      .filter(r -> REQUEST_STATUS_PENDING.equalsIgnoreCase(r.getRequestStatus()))
      .count();

    model.addAttribute("totalRequests", requests.getTotalElements());
    model.addAttribute("totalPendingRequests", totalPending);
    model.addAttribute("totalFinishedRequests", requests.getTotalElements() - totalPending);
  }
  private User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findUserByUsername(authentication.getName())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
  }

  private Employee getEmployeeForUser(User user) {
    return employeeRepository.getEmployeeByEmployeeId(user.getEmployee().getEmployeeId());
  }

  private void enrichWithSalaryRecords(RequestDTO requestDTO) {
    List<SalaryRecord> records = requestDTO.getPayrollIds().stream()
      .map(salaryRecordRepository::findSalaryRecordBySalaryId)
      .toList();
    requestDTO.setSalaryRecords(records);
  }
  /**
   * Hiển thị trang view requests với statistics và pagination
   * @param model: Model chứa thuộc tính
   * @param page số Trang hiện tại
   * @param size item mỗi trang
   * @return request.html
   */
  @RequestMapping
  public String request(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {
    return populateRequestModel(model, requestService.getRequests(getPageable(page, size)), "request");
  }

  /**
   * Hiển thị trang view requests đã được filter theo lựa chọn
   * @param model Model chứa thuộc tính
   * @param field: trường filtẻ
   * @param value: giá trị
   * @param page số Trang hiện tại
   * @param size item mỗi trang
   * @return request.html
   */
  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") String value,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "5") int size) {
    return populateRequestModel(model, requestService.filter(field, value, getPageable(page, size)), "request");
  }

  /**
   * Tìm kiếm request theo text
   * @param model Model chứa thuộc tính
   * @param query giá trị tìm kiếm
   * @param page số Trang hiện tại
   * @param size item mỗi trang
   * @return request.html
   */
  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("query") String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "5") int size) {
    Page<RequestDTO> requests = requestService.searchRequests(query, getPageable(page, size));

    model.addAttribute("requests", requests);
    model.addAttribute("query", query);
    addRequestStatistics(requests, model);
    return "request";
  }

  /**
   * Hiển thị trang export requests
   * @param model Model chứa thuộc tính
   * @param status Filter theo status
   * @param type Filter theo type
   * @param page số Trang hiện tại
   * @param size item mỗi trang
   * @return request-export.html
   */
  @RequestMapping("/export/view")
  public String exportView(Model model,
                           @RequestParam(value = "status", required = false, defaultValue = "all") String status,
                           @RequestParam(value = "type", required = false, defaultValue = "all") String type,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "5") int size) {
    Page<RequestDTO> requests = requestService.exportFilteredRequests(status, type, getPageable(page, size));

    model.addAttribute("requests", requests.getContent());
    model.addAttribute("totalRequests", requests.getTotalElements());
    model.addAttribute("requestTypes", requestService.getAllRequestTypes());
    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedType", type);
    return "request-export";
  }

  /**
   * Export ra file excels
   * @param status Filter theo status
   * @param type Filter theo type
   * @return JSON ResponseEntity.ok()
   */
  @RequestMapping("/export")
  public ResponseEntity<Resource> exportRequests(
    @RequestParam(value = "status", required = false, defaultValue = "all") String status,
    @RequestParam(value = "type", required = false, defaultValue = "all") String type) {
    log.info("Exporting request data to Excel. Status: {}, Type: {}", status, type);
    Resource file = requestService.exportToExcel(status, type);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=requests.xlsx")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(file);
  }

  /**
   * Lưu request được gửi bởi người dùng
   * @param requestDTO RequestDTO
   * @param result BindingResult, Validation (?)
   * @return redirect:/user/detail
   */
  @RequestMapping("/save")
  public String save(@ModelAttribute("requestDTO") RequestDTO requestDTO,
                     BindingResult result) {
    if (result.hasErrors()) {
      return "redirect:/user/detail";
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String name = authentication.getName();
    User user = userRepository.findUserByUsername(name)
      .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    int id = user.getEmployee().getEmployeeId();
    Employee employee = employeeRepository.getEmployeeByEmployeeId(id);


    requestService.save(requestDTO, user, employee);
    return "redirect:/user/detail?success=true";
  }

  @RequestMapping("/view")
  public String view(Model model, @RequestParam("requestId") Integer requestId) {
    RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
    List<SalaryRecord> salaryRecords = new ArrayList<>();
    for (var r : requestDTO.getPayrollIds()) {
      SalaryRecord sr = salaryRecordRepository.findSalaryRecordBySalaryId(r);
      salaryRecords.add(sr);
    }
    requestDTO.setSalaryRecords(salaryRecords);

    model.addAttribute("requestDTO", requestDTO);
    return "request-view";
  }

  @RequestMapping("/status")
  public String approveRequest(Model model,
                               @RequestParam("requestId") Integer requestId,
                               @RequestParam("field") String field,
                               @RequestParam("type") String type) {
    RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String name = authentication.getName();
    User user = userRepository.findUserByUsername(name)
      .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

    if ("Hạch toán lương".equals(type)) {
      switch (field) {
        case "approve":
          if ("pending".equals(requestDTO.getRequestStatus())) {
            requestDTO.setRequestStatus("approve");
            requestDTO.setApprovalName(user.getUsername());
            requestService.updateStatus(requestDTO, type);
          }
          break;
        case "deny":
          if ("pending".equals(requestDTO.getRequestStatus())) {
            requestDTO.setRequestStatus("deny");
            requestService.updateStatus(requestDTO, type);
          }
          break;
      }

    } else if ("Đơn xin nghỉ".equals(type)) {
      switch (field) {
        case "approve":
          if ("pending".equals(requestDTO.getRequestStatus())) {
            requestDTO.setRequestStatus("approve");
            requestDTO.setApprovalName(user.getUsername());
            requestService.updateStatus(requestDTO, type);
          }
          break;
        case "deny":
          if ("pending".equals(requestDTO.getRequestStatus())) {
            requestDTO.setRequestStatus("deny");
            requestService.updateStatus(requestDTO, type);
          }
          break;
      }

      var requests = requestService.getRequests(PageRequest.of(0, 10));
      totalRequests = 0;
      totalPendingRequests = 0;
      totalFinishedRequests = 0;
      for (var request : requests) {
        totalRequests++;
        if (request.getRequestStatus().equals("pending")) totalPendingRequests++;
        else totalFinishedRequests++;
        requestTypes.add(request.getRequestType());
      }


      model.addAttribute("totalRequests", totalRequests);
      model.addAttribute("totalPendingRequests", totalPendingRequests);
      model.addAttribute("totalFinishedRequests", totalFinishedRequests);
      model.addAttribute("requestDTO", requestDTO);
      model.addAttribute("requests", requests);
      return "request";
    }
    return "request";
  }
}
