package com.se1873.js.springboot.project.service.salary_record.command;

import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import org.springframework.stereotype.Service;

@Service
public class SalaryRecordCommandServiceImpl implements SalaryRecordCommandService {
  private final SalaryRecordRepository salaryRecordRepository;

  public SalaryRecordCommandServiceImpl(SalaryRecordRepository salaryRecordRepository) {
    this.salaryRecordRepository = salaryRecordRepository;
  }

  @Override
  public void removeSalaryRecord(SalaryRecord sr) {
    sr.setIsDeleted(true);
    salaryRecordRepository.save(sr);
  }
}
