package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestController {
  private final RequestService requestService;


  @RequestMapping
  public String request(Model model) {
  var requests = requestService.getRequests(PageRequest.of(0, 10));
    model.addAttribute("requests", requests);
    return "request";
  }

  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam("field") String field,
                       @RequestParam("value") String value) {
    List<String> requestTypes = requestService.getAllRequestTypes();
  log.info("filter");
  log.info("field: " + field);
  log.info("value: " + value);

    var requests = requestService.filter(field, value, PageRequest.of(0, 10));
    model.addAttribute("requests", requests);
    model.addAttribute("field", field);
    model.addAttribute("value", value);
    model.addAttribute("requestTypes", requestTypes);
    return "request";
  }

}
