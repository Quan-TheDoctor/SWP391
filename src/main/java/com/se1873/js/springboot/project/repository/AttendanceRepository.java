package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    @Query("from Attendance a ORDER BY a.date asc")
    List<Attendance> findAttendances();
    List<Attendance> findAttendancesByDate(LocalDate date);

    @Query("SELECT a FROM Attendance a " +
      "JOIN Employee e ON e.employeeId = a.employee.employeeId " +
      "JOIN EmploymentHistory eh ON eh.employee.employeeId = e.employeeId " +
      "JOIN Department d ON eh.department.departmentId = d.departmentId " +
      "WHERE eh.isCurrent = true AND d.departmentId = :value " +
      "ORDER BY a.date ASC")
    Page<Attendance> findAttendancesByFilters(String field, @Param("value") Integer value, Pageable pageable);


    @Query("SELECT a FROM Attendance a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month")
    Page<Attendance> findByMonthAndYear(
      @Param("year") int year,
      @Param("month") int month,
      Pageable pageable
    );
}
