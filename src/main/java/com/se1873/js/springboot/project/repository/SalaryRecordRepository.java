package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

  @Query("SELECT sr FROM SalaryRecord sr " +
    "WHERE (sr.year > :startYear OR (sr.year = :startYear AND sr.month >= :startMonth)) " +
    "AND (sr.year < :endYear OR (sr.year = :endYear AND sr.month <= :endMonth))")
  Page<SalaryRecord> findByYearMonthRange(
    @Param("startYear") int startYear,
    @Param("startMonth") int startMonth,
    @Param("endYear") int endYear,
    @Param("endMonth") int endMonth,
    Pageable pageable
  );

  @Query("SELECT sr FROM SalaryRecord sr " +
    "WHERE (sr.baseSalary > ?1) AND (sr.baseSalary <= ?2)")
  Page<SalaryRecord> findByRangeBaseSalary(double startRange, double baseSalary, Pageable pageable);

  Page<SalaryRecord> findByPaymentStatus(String paymentStatus, Pageable pageable);
}
