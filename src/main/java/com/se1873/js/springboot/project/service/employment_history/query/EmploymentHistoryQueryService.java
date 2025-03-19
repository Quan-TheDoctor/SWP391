package com.se1873.js.springboot.project.service.employment_history.query;

import com.se1873.js.springboot.project.entity.EmploymentHistory;

public interface EmploymentHistoryQueryService {
  EmploymentHistory findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(Integer employeeId, Boolean isCurrent);
}
