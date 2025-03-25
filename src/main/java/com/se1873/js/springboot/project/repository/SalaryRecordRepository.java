package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.AverageSalaryDTO;
import com.se1873.js.springboot.project.dto.TopSalaryDTO;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

  SalaryRecord findSalaryRecordBySalaryIdAndIsDeleted(Integer salaryId, boolean isDeleted);

  Page<SalaryRecord> getSalaryRecordsByEmployee_EmployeeId(Integer employeeId, Pageable pageable);

  @Query("SELECT s FROM SalaryRecord s WHERE s.employee.employeeId = :employeeId " +
    "AND s.month = :month " +
    "AND s.year = :year " +
    "AND s.isDeleted = false")
  Page<SalaryRecord> findSalaryRecordsByEmployeeAndMonthAndYear(Pageable pageable,
                                                                @Param("employeeId") Integer employeeId,
                                                                @Param("month") Integer month,
                                                                @Param("year") Integer year);

  Page<SalaryRecord> getSalaryRecordsByEmployee_EmployeeIdAndIsDeleted(Integer employeeEmployeeId, Boolean isDeleted, Pageable pageable);

  @Query("SELECT s.month, avg(s.baseSalary) FROM SalaryRecord s " +
    "WHERE s.year = :year GROUP BY s.month ORDER BY s.month")
  List<Object[]> getAverageSalaryByMonth(@Param("year") int year);

  @Query("SELECT s.month, SUM(s.netSalary) FROM SalaryRecord s " +
    "WHERE s.year = :year AND s.isDeleted = false GROUP BY s.month ORDER BY s.month")
  List<Object[]> getTotalSalaryByMonth(@Param("year") int year);

  Page<SalaryRecord> findAllByIsDeleted(Boolean isDeleted, Pageable pageable);

  Page<SalaryRecord> findSalaryRecordsByMonthAndYear(Pageable pageable, Integer month, Integer year);


  @Query("SELECT new com.se1873.js.springboot.project.dto.AverageSalaryDTO(s.year, s.month, d.departmentName, AVG(s.netSalary)) " +
    "FROM SalaryRecord s " +
    "JOIN EmploymentHistory eh ON s.employee.employeeId = eh.employee.employeeId " +
    "JOIN Department d ON eh.department.departmentId = d.departmentId " +
    "WHERE s.year = :year " +
    "GROUP BY s.year, s.month, d.departmentName " +
    "ORDER BY s.year, s.month")
  List<AverageSalaryDTO> getAverageSalaryByMonthAndDepartment(@Param("year") int year);

  @Query("SELECT new com.se1873.js.springboot.project.dto.TopSalaryDTO(e.firstName, e.lastName, p.positionName, sr.netSalary) " +
    "FROM SalaryRecord sr " +
    "JOIN sr.employee e " +
    "JOIN EmploymentHistory eh ON e.employeeId = eh.employee.employeeId " +
    "JOIN Position p ON eh.position.positionId = p.positionId " +
    "WHERE sr.month = :month AND sr.year = :year " +
    "AND eh.isCurrent = true " +
    "ORDER BY sr.netSalary DESC LIMIT 3")
  List<TopSalaryDTO> findTop3Salaries(@Param("month") int month, @Param("year") int year);

  SalaryRecord findSalaryRecordBySalaryId(Integer salaryId);

  List<SalaryRecord> findByMonthAndYearAndIsDeleted(Integer month, Integer year, Boolean isDeleted);


  SalaryRecord findSalaryRecordByEmployee_EmployeeIdAndMonthAndYearAndIsDeleted(Integer employeeEmployeeId, Integer month, Integer year, Boolean isDeleted);

  List<SalaryRecord> findByYearAndIsDeleted(Integer year, Boolean isDeleted);

  List<SalaryRecord> findSalaryRecordsByIsDeleted(Boolean isDeleted);

  Page<SalaryRecord> findSalaryRecordsByIsDeleted(Boolean isDeleted, Pageable pageable);

  List<AverageSalaryDTO> getAverageSalaryByYear(Integer year);

  @Query("SELECT SUM(s.netSalary) FROM SalaryRecord s WHERE s.isDeleted = false AND s.paymentStatus = 'Paid'")
  Double getTotalNetSalary();

  @Query("SELECT SUM(s.netSalary) FROM SalaryRecord s WHERE s.isDeleted = false AND s.paymentStatus = 'Pending'")
  Double getTotalUnpaidSalary();

  @Query("SELECT SUM(s.insuranceDeduction) FROM SalaryRecord s WHERE s.isDeleted = false AND s.paymentStatus = 'Paid'")
  Double getTotalCompanyTaxContributions();

  @Query("SELECT s.month, SUM(s.netSalary) FROM SalaryRecord s WHERE s.isDeleted = false AND s.paymentStatus = 'Paid' GROUP BY s.month ORDER BY s.month")
  List<Object[]> getMonthlyNetSalaryData();

  @Query("SELECT s.month, SUM(s.insuranceDeduction) FROM SalaryRecord s WHERE s.isDeleted = false AND s.paymentStatus = 'Paid' GROUP BY s.month ORDER BY s.month")
  List<Object[]> getMonthlyDeductionsData();
}
