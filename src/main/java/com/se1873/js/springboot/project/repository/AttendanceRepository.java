package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findAttendancesByEmployee_EmployeeId(Integer employeeEmployeeId);

    @Query("from Attendance a ORDER BY a.date asc")
    List<Attendance> findAttendances();
    List<Attendance> findAttendancesByDate(LocalDate date);
}
