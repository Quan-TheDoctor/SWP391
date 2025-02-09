package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
  Contract findContractByEmployee_EmployeeIdAndIsPresent(Integer employeeEmployeeId, boolean isPresent);
}
