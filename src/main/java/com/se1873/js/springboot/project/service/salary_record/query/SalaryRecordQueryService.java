package com.se1873.js.springboot.project.service.salary_record.query;

import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SalaryRecordQueryService {
  SalaryRecord findSalaryRecordBySalaryId(Integer salaryId);
  Page<SalaryRecord> getAll(Pageable pageable);
  List<PayrollDTO> filterPayrolls(List<PayrollDTO> payrolls, String[] field, String[] dates, String[] value);
  double calculateTotalNetSalary(List<PayrollDTO> payrolls);
  double calculateUnpaidSalary(List<PayrollDTO> payrolls);


}
