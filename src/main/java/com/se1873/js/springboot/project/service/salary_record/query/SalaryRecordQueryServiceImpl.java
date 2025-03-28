package com.se1873.js.springboot.project.service.salary_record.query;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaryRecordQueryServiceImpl implements SalaryRecordQueryService {
  private final SalaryRecordRepository salaryRecordRepository;

  public SalaryRecordQueryServiceImpl(SalaryRecordRepository salaryRecordRepository) {
    this.salaryRecordRepository = salaryRecordRepository;
  }

  @Override
  public SalaryRecord findSalaryRecordBySalaryId(Integer salaryId) {
    return salaryRecordRepository.findSalaryRecordBySalaryIdAndIsDeleted(salaryId, false);
  }

  @Override
  public List<SalaryRecord> findAll() {
    return salaryRecordRepository.findAll();
  }

  @Override
  public Page<SalaryRecord> getAll(Pageable pageable) {
    return salaryRecordRepository.findAllByIsDeleted(false, pageable);
  }

  @Override
  public List<PayrollDTO> filterPayrolls(List<PayrollDTO> payrolls, String[] field, String[] dates, String[] value) {
    if (field == null || field.length == 0 ||
      (value != null && "all".equals(value[0]) && !"startSalary".equals(field[0]))) return payrolls;

    if (isDateRangeFilter(field)) return filterByDateRange(payrolls, dates);

    if (isSalaryRangeFilter(field)) return filterBySalaryRange(payrolls, value);

    if (isPaymentStatusFilter(field)) return filterByPaymentStatus(payrolls, value);

    return payrolls;
  }

  @Override
  public double calculateTotalNetSalary(List<PayrollDTO> payrolls) {
    return payrolls.stream()
      .mapToDouble(PayrollDTO::getSalaryRecordNetSalary)
      .sum();
  }

  @Override
  public double calculateUnpaidSalary(List<PayrollDTO> payrolls) {
    return payrolls.stream()
      .filter(payroll -> "Pending".equals(payroll.getSalaryRecordPaymentStatus()))
      .mapToDouble(PayrollDTO::getSalaryRecordNetSalary)
      .sum();
  }

  private boolean isDateRangeFilter(String[] field) {
    return field.length >= 2 && "startDate".equals(field[0]) && "endDate".equals(field[1]);
  }

  private List<PayrollDTO> filterByDateRange(List<PayrollDTO> payrolls, String[] dates) {
    if (dates == null || dates.length < 2) {
      return payrolls;
    }

    LocalDate startDate = LocalDate.parse(dates[0]);
    LocalDate endDate = LocalDate.parse(dates[1]);

    return payrolls.stream()
      .filter(payroll -> isPayrollInDateRange(payroll, startDate, endDate))
      .toList();
  }

  private boolean isPayrollInDateRange(PayrollDTO payroll, LocalDate startDate, LocalDate endDate) {
    LocalDate payrollDate = LocalDate.of(
      payroll.getSalaryRecordYear(),
      payroll.getSalaryRecordMonth(),
      1
    );

    return payrollDate.isAfter(startDate.minusDays(1)) &&
      payrollDate.isBefore(endDate.plusDays(1));
  }

  private boolean isSalaryRangeFilter(String[] field) {
    return field.length >= 2 && "startSalary".equals(field[0]) && "endSalary".equals(field[1]);
  }

  private List<PayrollDTO> filterBySalaryRange(List<PayrollDTO> payrolls, String[] value) {
    if (value == null || value.length < 2 || "all".equals(value[0])) {
      return payrolls;
    }

    Double startSalary = Double.parseDouble(value[0]);
    Double endSalary = Double.parseDouble(value[1]);

    return payrolls.stream()
      .filter(payroll -> {
        Double totalSalary = payroll.getTotalNetSalary();
        return totalSalary >= startSalary && totalSalary <= endSalary;
      })
      .toList();
  }

  private boolean isPaymentStatusFilter(String[] field) {
    return field.length >= 1 && "paymentStatus".equals(field[0]);
  }

  private List<PayrollDTO> filterByPaymentStatus(List<PayrollDTO> payrolls, String[] value) {
    if (value == null || value.length < 1) {
      return payrolls;
    }

    return payrolls.stream()
      .filter(payroll -> payroll.getSalaryRecordPaymentStatus().equals(value[0]))
      .collect(Collectors.toList());
  }
}
