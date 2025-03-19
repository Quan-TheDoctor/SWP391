package com.se1873.js.springboot.project.service.contract.query;

import com.se1873.js.springboot.project.entity.Contract;

public interface ContractQueryService {
  Contract findContractByEmployee_EmployeeIdAndPresent(Integer employeeId, Boolean isPresent);
}
