package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  List<Attendance> getAttendanceByDateBetweenAndEmployee_EmployeeId(LocalDate dateAfter, LocalDate dateBefore, Integer employeeEmployeeId);
  Optional<Attendance> findByDateAndEmployee_EmployeeId(LocalDate date, Integer employeeId);
  Page<Attendance> getAttendanceByEmployee_EmployeeId(Integer employeeId, Pageable pageable);

  @Query("SELECT a FROM Attendance a WHERE a.date = :date")
  List<Attendance> findAttendancesByDate(@Param("date") LocalDate date);

  @Query("SELECT a FROM Attendance a WHERE a.employee.employeeId = :employeeId " +
          "AND EXTRACT(MONTH FROM a.date) = :month " +
          "AND EXTRACT(YEAR FROM a.date) = :year")
  Page<Attendance> findAttendancesByEmployeeAndMonthAndYear(Pageable pageable,
                                                            @Param("employeeId") Integer employeeId,
                                                            @Param("month") Integer month,
                                                            @Param("year") Integer year);


  @Query("SELECT a FROM Attendance a WHERE a.employee.employeeId = :employeeId AND a.status = :status")
  Page<Attendance> findAttendancesByEmployeeIdAndStatus(Pageable pageable,
                                                      @Param("employeeId") Integer employeeId,
                                                      @Param("status") String status);


  Page<Attendance> findAttendancesByStatus(Pageable pageable, String status);

  List<Attendance> findAllByEmployee_EmployeeId(Integer employeeId);

  @Query(value = """
        SELECT a.*, CONCAT(e.first_name, ' ', e.last_name) AS full_name
        FROM attendance a
        JOIN employees e ON a.employee_id = e.employee_id
        WHERE EXISTS (
            SELECT 1
            FROM unnest(string_to_array(lower(:searchText), ' ')) AS keyword
            WHERE lower(CONCAT(e.first_name, ' ', e.last_name)) ILIKE '%' || keyword || '%'
        )
        ORDER BY a.date DESC
    """, nativeQuery = true)
  Page<Attendance> searchAttendanceByEmployeeName(@Param("searchText") String searchText, Pageable pageable);

  List<Attendance>  findAllByDateBetween(LocalDate dateAfter, LocalDate dateBefore);

  Attendance findByAttendanceId(Integer attendanceId);
  @Query("SELECT a FROM Attendance a WHERE a.employee.employeeId IN :employeeIds")
  List<Attendance> findAllByEmployee_EmployeeIdIn(@Param("employeeIds") List<Integer> employeeIds);
  @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate")
  List<Attendance> findAttendancesByDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);



  @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate AND a.status = :status")
  Page<Attendance> findAttendancesByStatusAndDateRange(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate,
                                                       @Param("status") String status,
                                                       Pageable pageable);
  @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate")
  Page<Attendance> findAttendancesByDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              Pageable pageable);
}
