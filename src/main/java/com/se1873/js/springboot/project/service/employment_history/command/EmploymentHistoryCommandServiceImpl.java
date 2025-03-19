package com.se1873.js.springboot.project.service.employment_history.command;

import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.repository.EmploymentHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class EmploymentHistoryCommandServiceImpl implements EmploymentHistoryCommandService {

  private final EmploymentHistoryRepository employmentHistoryRepository;

  public EmploymentHistoryCommandServiceImpl(EmploymentHistoryRepository employmentHistoryRepository) {
    this.employmentHistoryRepository = employmentHistoryRepository;
  }

  @Override
  public void save(EmploymentHistory employmentHistory) {
    employmentHistoryRepository.save(employmentHistory);
  }
}
