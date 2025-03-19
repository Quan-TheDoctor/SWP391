package com.se1873.js.springboot.project.service.employment_history;


import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Department;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.entity.Position;
import com.se1873.js.springboot.project.repository.EmploymentHistoryRepository;
import com.se1873.js.springboot.project.service.employment_history.command.EmploymentHistoryCommandService;
import com.se1873.js.springboot.project.service.employment_history.query.EmploymentHistoryQueryService;
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
  private final EmploymentHistoryCommandService employmentHistoryCommandService;
  private final EmploymentHistoryQueryService employmentHistoryQueryService;

  public void save(EmploymentHistory employmentHistory) {
    employmentHistoryCommandService.save(employmentHistory);
  }

  public EmploymentHistory findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(Integer employeeId, Boolean isCurrent) {
    return employmentHistoryQueryService.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employeeId, isCurrent);
  }

  public EmploymentHistory getEmploymentHistoryByEmployeeId(Integer employeeId) {
    return employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employeeId, true);
  }
}
