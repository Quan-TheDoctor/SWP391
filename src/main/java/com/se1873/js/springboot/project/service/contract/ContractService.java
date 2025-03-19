package com.se1873.js.springboot.project.service.contract;

import com.se1873.js.springboot.project.entity.Contract;
import com.se1873.js.springboot.project.service.contract.command.ContractCommandService;
import com.se1873.js.springboot.project.service.contract.query.ContractQueryService;
import org.springframework.stereotype.Service;

@Service
public class ContractService {
  private final ContractQueryService contractQueryService;
  private final ContractCommandService contractCommandService;

  public ContractService(ContractQueryService contractQueryService, ContractCommandService contractCommandService) {
    this.contractQueryService = contractQueryService;
    this.contractCommandService = contractCommandService;
  }

  public Contract findContractByEmployee_EmployeeIdAndPresent(Integer employeeId, Boolean isPresent) {
    return contractQueryService.findContractByEmployee_EmployeeIdAndPresent(employeeId, isPresent);
  }

  public void save(Contract contract) {
    contractCommandService.save(contract);
  }
}
