package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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
  Double totalNetSalary = 0.0;
  Double unpaidSalary = 0.0;
  Page<PayrollDTO> payrolls = null;

  @RequestMapping
  public String payroll(Model model,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "size", defaultValue = "5") Integer size) {
    Pageable pageable = PageRequest.of(page, size);
    totalNetSalary = 0.0;
    unpaidSalary = 0.0;
    var pays = salaryRecordService.findAlls();
    for (PayrollDTO p : pays) {
      if (p.getSalaryRecord().getPaymentStatus().equals("Đã thanh toán"))
        totalNetSalary += p.getSalaryRecord().getNetSalary();
      if (p.getSalaryRecord().getPaymentStatus().equals("Chưa thanh toán"))
        unpaidSalary += p.getSalaryRecord().getNetSalary();
    }

    payrolls = salaryRecordService.findAll(pageable);

    Page<PayrollDTO> payrollDTOPage = payrolls;

    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("payrolls", payrollDTOPage);
    return "fragments/payroll";
  }

  @RequestMapping("/create-form")
  public String createForm(Model model) {

    return "fragments/payroll-create";
  }

  @RequestMapping("/sort")
  public String sort(Model model,
                     @RequestParam(value = "field", required = false, defaultValue = "all") String field,
                     @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
                     @RequestParam(value = "dates", required = false) LocalDate dates,
                     @RequestParam(value = "page", defaultValue = "0") Integer page,
                     @RequestParam(value = "size", defaultValue = "5") Integer size) {
    List<PayrollDTO> payrollDTOS = salaryRecordService.sortByField(field, direction, payrolls.getContent());
    Pageable pageable = PageRequest.of(page, size);

    Page<PayrollDTO> payrollDTOPage = new PageImpl<>(payrollDTOS, pageable, payrolls.getTotalElements());
    getTotalNetSalary(payrolls);

    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("payrolls", payrollDTOPage);
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
                       @RequestParam(value = "size", defaultValue = "5") Integer size) {
    Pageable pageable = null;
    List<String> fields = List.of(field.split(","));
    String[] dates = date.split(",");

    if ("all".equals(value)) {
      pageable = PageRequest.of(page, size);
      payrolls = salaryRecordService.findAll(pageable);
    } else if ("startDate,endDate".equals(field)) {
      pageable = PageRequest.of(page, size);
      LocalDate startDate = parseDate(dates[0].trim());
      LocalDate endDate = parseDate(dates[1].trim());

      if (startDate == null || endDate == null) {
        throw new IllegalArgumentException("Invalid date parameters");
      }
      payrolls = salaryRecordService.findByStartDateAndEndDate(startDate, endDate, pageable);
    } else if ("startSalary,endSalary".equals(field)) {
      String[] values = value.split(",");
      Sort sort = direction.equals("asc") ? Sort.by(Sort.Direction.ASC, "baseSalary") : Sort.by(Sort.Direction.DESC, "baseSalary");
      pageable = PageRequest.of(page, size, sort);

      payrolls = salaryRecordService.findByRangeBaseSalary(Double.parseDouble(values[0]), Double.parseDouble(values[1]), pageable);
    } else if ("paymentStatus".equals(field)) {
      if (value == null) {
        throw new IllegalArgumentException("Invalid payment status");
      }
      pageable = PageRequest.of(page, size);
      payrolls = salaryRecordService.findByPaymentStatus(value, pageable);
    }

    List<PayrollDTO> payrollDTOS = payrolls.getContent();
    Page<PayrollDTO> payrollDTOPage = new PageImpl<>(payrollDTOS, pageable, payrolls.getTotalElements());

    getTotalNetSalary(payrolls);

    model.addAttribute("totalNetSalary", totalNetSalary);
    model.addAttribute("unpaidSalary", unpaidSalary);
    model.addAttribute("payrolls", payrollDTOPage);
    model.addAttribute("field", field);
    model.addAttribute("value", value);
    model.addAttribute("direction", direction);
    return "fragments/payroll";
  }

  private void getTotalNetSalary(Page<PayrollDTO> payrolls) {
    totalNetSalary = 0.0;
    unpaidSalary = 0.0;

    for (PayrollDTO p : payrolls) {
      if (p.getSalaryRecord().getPaymentStatus().equals("Đã thanh toán"))
        totalNetSalary += p.getSalaryRecord().getNetSalary();
      if (p.getSalaryRecord().getPaymentStatus().equals("Chưa thanh toán"))
        unpaidSalary += p.getSalaryRecord().getNetSalary();
    }
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