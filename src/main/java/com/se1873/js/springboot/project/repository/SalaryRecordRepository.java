package com.se1873.js.springboot.project.repository;

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

    SalaryRecord findSalaryRecordBySalaryId(Integer salaryId);
    Page<SalaryRecord> getSalaryRecordsByEmployee_EmployeeId(Integer employeeId, Pageable pageable);

    @Query("SELECT s FROM SalaryRecord s WHERE s.employee.employeeId = :employeeId " +
            "AND s.month = :month " +
            "AND s.year = :year")
    Page<SalaryRecord> findSalaryRecordsByEmployeeAndMonthAndYear(Pageable pageable,
                                                                  @Param("employeeId") Integer employeeId,
                                                                  @Param("month") Integer month,
                                                                  @Param("year") Integer year);


    @Query("SELECT s.month, SUM(s.netSalary) FROM SalaryRecord s " +
            "WHERE s.year = :year GROUP BY s.month ORDER BY s.month")
    List<Object[]> getTotalSalaryByMonth(@Param("year") int year);

    Page<SalaryRecord> findSalaryRecordsByMonthAndYear(Pageable pageable,Integer month,Integer year);
}
