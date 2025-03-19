package com.se1873.js.springboot.project.service.employment_history.query;

import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.repository.EmploymentHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class EmploymentHistoryQueryServiceImpl implements EmploymentHistoryQueryService {
  private final EmploymentHistoryRepository employmentHistoryRepository;

  public EmploymentHistoryQueryServiceImpl(EmploymentHistoryRepository employmentHistoryRepository) {
    this.employmentHistoryRepository = employmentHistoryRepository;
  }

  @Override
  public EmploymentHistory findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(Integer employeeId, Boolean isCurrent) {
    return employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employeeId, isCurrent);
  }
}
