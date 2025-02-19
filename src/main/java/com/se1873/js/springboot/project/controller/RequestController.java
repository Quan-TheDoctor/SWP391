package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    var totalRequests = requests.getTotalElements();
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

}
