package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
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


  @Query("SELECT a FROM Attendance a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month " +
    "AND a.employee.employeeId = :employeeId")
  List<Attendance> findAttendancesByEmployeeIdAndSpecificTime(@Param("employeeId") Integer employeeEmployeeId,
                                                        @Param("year") int year,
                                                        @Param("month") int month);

  List<Attendance> findAttendancesByEmployee_EmployeeId(Integer employeeEmployeeId);

  @Query("SELECT a FROM Attendance a JOIN a.employee e " +
          "WHERE LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', REPLACE(:Name, ' ', '%'), '%')) " +
          "ORDER BY e.employeeId ASC")
  Page<Attendance> searchAttendanceByEmployeeName(@Param("Name") String Name, Pageable pageable);
//chạy thử xem?

}
