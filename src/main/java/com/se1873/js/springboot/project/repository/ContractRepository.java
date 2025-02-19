package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
  @Query("SELECT c FROM Contract c WHERE c.employee.employeeId = ?1 AND c.isPresent = ?2")
  Contract findContractByEmployee_EmployeeIdAndPresent(Integer employeeEmployeeId, boolean present);
}
