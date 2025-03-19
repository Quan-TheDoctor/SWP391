package com.se1873.js.springboot.project.service.contract.command;

import com.se1873.js.springboot.project.entity.Contract;
import com.se1873.js.springboot.project.repository.ContractRepository;
import org.springframework.stereotype.Service;

@Service
public class ContractCommandServiceImpl implements ContractCommandService {
  private final ContractRepository contractRepository;

  public ContractCommandServiceImpl(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  @Override
  public void save(Contract contract) {
    contractRepository.save(contract);
  }
}
