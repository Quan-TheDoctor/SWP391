package com.se1873.js.springboot.project.service;


import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.repository.EmploymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EmploymentHistoryService {

  private final EmploymentHistoryRepository employmentHistoryRepository;

  public EmploymentHistory getEmploymentHistoryByEmployeeId(Integer employeeId) {
    return employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employeeId, true);
  }
}
