package com.se1873.js.springboot.project.service.contract.query;

import com.se1873.js.springboot.project.entity.Contract;
import com.se1873.js.springboot.project.repository.ContractRepository;
import org.springframework.stereotype.Service;

@Service
public class ContractQueryServiceImpl implements ContractQueryService {
  private final ContractRepository contractRepository;

  public ContractQueryServiceImpl(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  @Override
  public Contract findContractByEmployee_EmployeeIdAndPresent(Integer employeeId, Boolean isPresent) {
    return contractRepository.findContractByEmployee_EmployeeIdAndPresent(employeeId, true);
  }
}
