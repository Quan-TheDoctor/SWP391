package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payroll")
@Slf4j
public class PayrollController {
  private final SalaryRecordService salaryRecordService;

  @RequestMapping
  public String payroll(Model model) {
    Pageable page = PageRequest.of(0, 10);
    Page<PayrollDTO> payrolls = salaryRecordService.findAll(page);
    log.info(payrolls.toString());
    model.addAttribute("payrolls", payrolls);
    return "fragments/payroll";
  }

  @RequestMapping("/sort")
  public String sort(Model model,
                     @RequestParam(value = "field", required = false, defaultValue = "all") String field,
                     @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
                     @RequestParam(value = "dates", required = false) LocalDate dates,
                     @RequestParam(value = "page", defaultValue = "0") Integer page,
                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
    Pageable pageable = null;
    List<String> fields = List.of(field.split(","));
    log.info(fields.toString());
    if ("all".equals(fields.get(0))) {
      pageable = PageRequest.of(page, size);
    } else if ("month,year".equals(field) || "deductions,insuranceDeduction".equals(field)) {
      Sort sort = direction.equals("asc") ?
        Sort.by(Sort.Order.asc(fields.get(1)), Sort.Order.asc(fields.get(0)))
        : Sort.by(Sort.Order.desc(fields.get(1)), Sort.Order.desc(fields.get(0)));
      pageable = PageRequest.of(page, size, sort);
    } else if ("netSalary".equals(field) || "baseSalary".equals(field) || "employee.firstName".equals(field) || "paymentStatus".equals(field)) {
      Sort sort = direction.equals("asc") ? Sort.by(Sort.Direction.ASC, fields.get(0)) : Sort.by(Sort.Direction.DESC, fields.get(0));
      pageable = PageRequest.of(page, size, sort);
    }

    Page<PayrollDTO> payrolls = salaryRecordService.findAll(pageable);

    model.addAttribute("payrolls", payrolls);
    model.addAttribute("field", field);
    model.addAttribute("direction", direction);
    return "fragments/payroll";
  }


  @RequestMapping("/filter")
  public String filter(Model model,
                       @RequestParam(value = "field", required = false, defaultValue = "all") String field,
                       @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
                       @RequestParam(value = "dates", required = false, defaultValue = "all") String date,
                       @RequestParam(value = "value", defaultValue = "0") String value,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
    Pageable pageable = null;
    Page<PayrollDTO> payrolls = null;
    List<String> fields = List.of(field.split(","));
    String[] dates = date.split(",");
    log.info(date);
    log.info(fields.toString());
    if ("all".equals(fields.get(0))) {
      pageable = PageRequest.of(page, size);
      payrolls = salaryRecordService.findAll(pageable);
    } else if("startDate,endDate".equals(field)) {
      pageable = PageRequest.of(page, size);
      LocalDate startDate = parseDate(dates[0].trim());
      LocalDate endDate = parseDate(dates[1].trim());

      if (startDate == null || endDate == null) {
        throw new IllegalArgumentException("Invalid date parameters");
      }
      payrolls = salaryRecordService.findByStartDateAndEndDate(startDate, endDate, pageable);
    } else if("startSalary,endSalary".equals(field)) {
      String[] values = value.split(",");
      Sort sort = direction.equals("asc") ? Sort.by(Sort.Direction.ASC, "baseSalary") : Sort.by(Sort.Direction.DESC, "baseSalary");
      pageable = PageRequest.of(page, size, sort);

      payrolls = salaryRecordService.findByRangeBaseSalary(Double.parseDouble(values[0]), Double.parseDouble(values[1]), pageable);
    } else if("paymentStatus".equals(field)) {
      if(value == null) {
        throw new IllegalArgumentException("Invalid payment status");
      }
      pageable = PageRequest.of(page, size);
      payrolls = salaryRecordService.findByPaymentStatus(value, pageable);
    }


    model.addAttribute("payrolls", payrolls);
    model.addAttribute("field", field);
    model.addAttribute("value", value);
    model.addAttribute("direction", direction);
    return "fragments/payroll";
  }
  private LocalDate parseDate(String dateString) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
        Locale.ENGLISH
      );

      String normalizedDate = dateString.replace("GMT ", "GMT+");

      ZonedDateTime zonedDateTime = ZonedDateTime.parse(normalizedDate, formatter);
      return zonedDateTime.toLocalDate();

    } catch (DateTimeParseException ex1) {
      try {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
      } catch (DateTimeParseException ex2) {
        log.error("Error parsing date: {}", dateString);
        throw new IllegalArgumentException("Invalid date format: " + dateString);
      }
    }
  }
}