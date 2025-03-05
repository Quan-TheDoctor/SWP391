package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  List<Attendance> getAttendanceByDateBetween(LocalDate dateAfter, LocalDate dateBefore);

  List<Attendance> getAttendanceByDateBetweenAndEmployee_EmployeeId(LocalDate dateAfter, LocalDate dateBefore, Integer employeeEmployeeId);
  Optional<Attendance> findByDateAndEmployee_EmployeeId(LocalDate date, Integer employeeId);
  Attendance getAttendanceByDateAndEmployee_EmployeeId(LocalDate date, Integer employeeEmployeeId);

  Attendance getAttendanceByAttendanceId(Integer attendanceId);
  Page<Attendance> getAttendanceByEmployee_EmployeeId(Integer employeeId, Pageable pageable);

  @Query("SELECT a FROM Attendance a WHERE a.date = :date")
  List<Attendance> findAttendancesByDate(@Param("date") LocalDate date);
}
