package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.Request;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestController {
  private final RequestService requestService;
  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;

  private Integer totalRequests = 0;
  private Integer totalPendingRequests = 0;
  private Integer totalFinishedRequests = 0;
  private Set<String> requestTypes = new HashSet<>();
  @RequestMapping
  public String request(Model model) {
    var requests = requestService.getRequests(PageRequest.of(0, 10));
    totalRequests = 0;
    totalPendingRequests = 0;
    totalFinishedRequests = 0;
    for(var request : requests) {
      totalRequests++;
      if(request.getRequestStatus().equals("pending")) totalPendingRequests++;
      else totalFinishedRequests++;
      requestTypes.add(request.getRequestType());
    }



    model.addAttribute("totalRequests", totalRequests);
    model.addAttribute("totalPendingRequests", totalPendingRequests);
    model.addAttribute("totalFinishedRequests", totalFinishedRequests);
    model.addAttribute("requestTypes", requestTypes);
    model.addAttribute("requests", requests);
    return "request";
  }

  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") String value) {
    var requests = requestService.filter(field, value, PageRequest.of(0, 10));
    totalRequests = 0;
    totalPendingRequests = 0;
    totalFinishedRequests = 0;
    for(var request : requests) {
      totalRequests++;
      if(request.getRequestStatus().equals("pending")) totalPendingRequests++;
      else totalFinishedRequests++;
      requestTypes.add(request.getRequestType());
    }
    model.addAttribute("totalRequests", totalRequests);
    model.addAttribute("totalPendingRequests", totalPendingRequests);
    model.addAttribute("totalFinishedRequests", totalFinishedRequests);
    model.addAttribute("requests", requests);
    model.addAttribute("field", field);
    model.addAttribute("value", value);
    model.addAttribute("requestTypes", requestTypes);
    return "request";
  }
  @RequestMapping("/search")
  public String search(Model model,
                       @RequestParam("query") String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var requests = requestService.searchRequests(query, pageable);

    this.totalRequests = (int) requests.getTotalElements();
    long totalPendingRequests = requests.stream().filter(r -> "pending".equalsIgnoreCase(r.getRequestStatus())).count();
    long totalFinishedRequests = totalRequests - totalPendingRequests;

    Set<String> requestTypes = requests.stream().map(r -> r.getRequestType()).collect(Collectors.toSet());

    model.addAttribute("requests", requests);
    model.addAttribute("totalRequests", totalRequests);
    model.addAttribute("totalPendingRequests", totalPendingRequests);
    model.addAttribute("totalFinishedRequests", totalFinishedRequests);
    model.addAttribute("requestTypes", requestTypes);
    model.addAttribute("query", query);  // Lưu query để hiển thị lại trên UI

    return "request";
  }
  @RequestMapping("/export/view")
  public String exportView(Model model,
                           @RequestParam(value = "status", required = false, defaultValue = "all") String status,
                           @RequestParam(value = "type", required = false, defaultValue = "all") String type,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var requests = requestService.exportFilteredRequests(status, type, pageable);
    var totalRequests = requests.getTotalElements();

    model.addAttribute("totalRequests", totalRequests);
    model.addAttribute("currentPage", page + 1);
    model.addAttribute("totalPages", requests.getTotalPages());
    model.addAttribute("requestTypes", requestService.getAllRequestTypes());
    model.addAttribute("requests", requests.getContent());

    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedType", type);

    return "request-export";
  }

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
  @RequestMapping("/save")
  public String save(@ModelAttribute("requestDTO")RequestDTO requestDTO,
                     BindingResult result){

    if(result.hasErrors()){
      return "redirect:/user/detail";
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String name = authentication.getName();
    User user = userRepository.findUserByUsername(name)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    int id = user.getEmployee().getEmployeeId();
    Employee employee = employeeRepository.getEmployeeByEmployeeId(id);


    requestService.save(requestDTO,user,employee);
    return "redirect:/user/detail?success=true";
  }
}
