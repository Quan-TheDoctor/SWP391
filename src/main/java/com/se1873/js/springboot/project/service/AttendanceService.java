package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AttendanceCreationDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.EmploymentHistory;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

  private final AttendanceRepository attendanceRepository;
  private final EmployeeRepository employeeRepository;

  public Page<AttendanceDTO> getEmployeesAndAttendances(LocalDate startDate, LocalDate endDate, Pageable pageable) {
    try {
      Page<Employee> employees = employeeRepository.findAll(pageable);

      List<Attendance> allAttendances = new ArrayList<>();

      for (Employee e : employees.getContent()) {
        Page<Attendance> employeeAttendances = attendanceRepository.findAttendancesByEmployee_EmployeeIdAndDateBetween(
          e.getEmployeeId(),
          startDate,
          endDate,
          pageable
        );
        log.info(employeeAttendances.toString());

        if (employeeAttendances.isEmpty()) {
          Attendance newAttendance = createNewAttendance(e, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(17, 0));
          allAttendances.add(newAttendance);
        }
        else {
          allAttendances.addAll(employeeAttendances.getContent());
        }
      }

      Page<Attendance> attendancePage = new PageImpl<>(
        allAttendances,
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "attendanceDate")),
        allAttendances.size()
      );

      return attendancePage.map(this::convertAttendanceToAttendanceDTO);
    } catch (Exception e) {
      log.error("Error fetching attendances: {}", e.getMessage());
      return Page.empty();
    }
  }

  public Page<AttendanceDTO> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate, Pageable pageable) {
    return attendanceRepository.findAllByDateBetween(startDate, endDate, pageable).map(this::convertAttendanceToAttendanceDTO);
  }

  public Page<AttendanceDTO> getAll(Pageable pageable) {
    return attendanceRepository.findAll(pageable).map(this::convertAttendanceToAttendanceDTO);
  }

  public Page<AttendanceDTO> getAttendanceByMonth(int year, int month, int day, Pageable pageable) {
    Page<Attendance> attendancePage = attendanceRepository.findByMonthAndYear(year, month, day, pageable);
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
    return Optional.ofNullable(AttendanceDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeCode(employee.getEmployeeCode())
      .firstName(employee.getFirstName())
      .lastName(employee.getLastName())
      .birthDate(employee.getBirthDate())
      .gender(employee.getGender())
      .idNumber(employee.getIdNumber())
      .permanentAddress(employee.getPermanentAddress())
      .companyEmail(employee.getCompanyEmail())
      .phoneNumber(employee.getPhoneNumber())
      .maritalStatus(employee.getMaritalStatus())
      .bankAccount(employee.getBankAccount())
      .bankName(employee.getBankName())
      .taxCode(employee.getTaxCode())
      .attendanceId(attendance.getAttendanceId())
      .attendanceDate(attendance.getDate())
      .attendanceCheckIn(attendance.getCheckIn())
      .attendanceCheckOut(attendance.getCheckOut())
      .attendanceOvertimeHours(attendance.getOvertimeHours())
      .attendanceStatus(attendance.getStatus())
      .attendanceNote(attendance.getNote())
      .build()).orElse(null);
  }

  private Attendance createNewAttendance(Employee employee, LocalDate date, LocalTime checkIn, LocalTime checkOut) {
    Attendance attendance = new Attendance();
    attendance.setEmployee(employee);
    attendance.setDate(date);
    attendance.setCheckIn(checkIn);
    attendance.setCheckOut(checkOut);

    attendance.setStatus("Chưa chấm công");
    return attendance;
  }

  public AttendanceDTO getTodayAttendanceByEmployeeId(Integer employeeId, LocalDate date, LocalTime checkIn, LocalTime checkOut) {
    Employee employee = employeeRepository.findByEmployeeId(employeeId);
    log.info(employee.getEmployeeId().toString());
    Attendance attendance = Optional
      .ofNullable(attendanceRepository.findAttendanceByEmployee_EmployeeIdAndDate(employeeId, date))
      .orElse(createNewAttendance(employee, date, checkIn, checkOut));
    AttendanceDTO attendanceDTO = convertAttendanceToAttendanceDTO(attendance);
    return Optional.ofNullable(attendanceDTO).orElse(new AttendanceDTO());
  }

  public void updateOrCreateAttendances(AttendanceCreationDTO dtos) {
    for (var dto : dtos.getAttendances()) {
      Optional<Attendance> optionalAttendance = attendanceRepository
        .findByEmployee_EmployeeIdAndDate(
          dto.getEmployeeId(),
          dto.getAttendanceDate()
        );

      Attendance attendance;
      if (optionalAttendance.isEmpty()) {
        // Create new attendance if none exists
        attendance = new Attendance();
        Employee employee = Optional.ofNullable(employeeRepository.findByEmployeeId(dto.getEmployeeId()))
          .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getEmployeeId()));
        attendance.setEmployee(employee);
      } else {
        // Update existing attendance
        attendance = optionalAttendance.get();
      }

      log.info(attendance.toString());

      try {
        LocalDate date = dto.getAttendanceDate() != null ? LocalDate.of(
          dto.getAttendanceDate().getYear(),
          dto.getAttendanceDate().getMonthValue(),
          dto.getAttendanceDate().getDayOfMonth()
        ) : null;

        LocalTime checkIn = dto.getAttendanceCheckIn() != null ? LocalTime.of(
          dto.getAttendanceCheckIn().getHour(),
          dto.getAttendanceCheckIn().getMinute()
        ) : null;

        LocalTime checkOut = dto.getAttendanceCheckOut() != null ? LocalTime.of(
          dto.getAttendanceCheckOut().getHour(),
          dto.getAttendanceCheckOut().getMinute()
        ) : null;
        attendance.setDate(date);
        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);
        attendance.setStatus(dto.getAttendanceStatus());

        attendanceRepository.save(attendance);

      } catch (DateTimeException e) {
        log.error("Invalid datetime format for attendance ID {}: {}", dto.getAttendanceId(), e.getMessage());
      }
    }
  }
}
