package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

  private final AttendanceRepository attendanceRepository;

  public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
    List<Attendance> attendances = null;
    attendances = attendanceRepository.findAttendancesByDate(date);
    AttendanceDTO attendanceDTO = null;
    List<AttendanceDTO> attendanceDTOs = new ArrayList<>();
    for (Attendance attendance : attendances) {
      Employee employee = attendance.getEmployee();
      attendanceDTO = AttendanceDTO.builder()
        .employee(employee).attendance(attendance).build();
      log.info(attendanceDTO.toString());

      attendanceDTOs.add(attendanceDTO);
    }
    return attendanceDTOs;
  }

  public List<AttendanceDTO> getAllAttendance() {
    List<Attendance> attendances = null;
    attendances = attendanceRepository.findAttendances();
    AttendanceDTO attendanceDTO = null;
    List<AttendanceDTO> attendanceDTOs = new ArrayList<>();
    for (Attendance attendance : attendances) {
      Employee employee = attendance.getEmployee();
      attendanceDTO = AttendanceDTO.builder()
        .employee(employee).attendance(attendance).build();
      attendanceDTOs.add(attendanceDTO);
    }
    return attendanceDTOs;

  }

  public Page<AttendanceDTO> filterAttendancesByField(String field, Integer value, Pageable pageable) {
    Specification<Attendance> spec = (root, query, cb) -> {
      Join<Attendance, Employee> employeeJoin = root.join("employee");
      Join<Employee, EmploymentHistory> employmentHistoryJoin = employeeJoin.join("employmentHistories");

      Predicate isCurrentPredicate = cb.equal(employmentHistoryJoin.get("isCurrent"), true);

      if (field != null && value != null) {
        switch (field.toLowerCase()) {
          case "department":
            Predicate departmentPredicate = cb.equal(employmentHistoryJoin.get("department").get("id"), value);
            return cb.and(isCurrentPredicate, departmentPredicate);
          case "position":
            Predicate positionPredicate = cb.equal(employmentHistoryJoin.get("position").get("id"), value);
            return cb.and(isCurrentPredicate, positionPredicate);
          default:
            throw new IllegalArgumentException("Invalid filter field: " + field);
        }
      }
      return isCurrentPredicate;
    };

    Page<Attendance> attendances = attendanceRepository.findAll(spec, pageable);

    return attendances.map(this::convertAttendanceToAttendanceDTO);
  }

  public Page<AttendanceDTO> getAttendanceByMonth(int year, int month, Pageable pageable) {
    Page<Attendance> attendancePage = attendanceRepository.findByMonthAndYear(year, month, pageable);
    return attendancePage.map(this::convertAttendanceToAttendanceDTO);
  }

  public List<AttendanceDTO> findAttendancesByEmployeeId(Integer employeeId) {
    return attendanceRepository.findAttendancesByEmployee_EmployeeId(employeeId)
      .stream().map(this::convertAttendanceToAttendanceDTO).collect(Collectors.toList());
  }

  public List<AttendanceDTO> findAttendancesByEmployeeIdInSpecifictime(Integer employeeId, int year, int month) {
    return attendanceRepository.findAttendancesByEmployeeIdAndSpecificTime(employeeId, year, month)
      .stream().map(this::convertAttendanceToAttendanceDTO).collect(Collectors.toList());
  }

  private AttendanceDTO convertAttendanceToAttendanceDTO(Attendance attendance) {
    Employee employee = attendance.getEmployee();
    return AttendanceDTO.builder()
      .attendance(attendance)
      .employee(employee)
      .build();
  }

//  public Page<AttendanceDTO> search(String Name,Pageable pageable){
//
//    var employees = attendanceRepository.searchAttendanceByEmployeeName(Name,pageable);
//
//    Page<Attendance> attendances = null;
//
//    attendances = employees.stream().map(this::convertAttendanceToAttendanceDTO);
//    return null;
//  }
//
//  private AttendanceDTO convertEmployeeToAttendanceDTO(Employee employee) {
//
//    return EmployeeDTO
//  }
}
