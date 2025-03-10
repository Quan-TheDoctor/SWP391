package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    SalaryRecord findSalaryRecordBySalaryId(Integer salaryId);
    Page<SalaryRecord> getSalaryRecordsByEmployee_EmployeeId(Integer employeeId, Pageable pageable);
}
