package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.AttendanceCountDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.AttendanceDTOList;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.mapper.AttendanceDTOMapper;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AttendanceService {

  private final EmployeeService employeeService;
  private final AttendanceRepository attendanceRepository;
  private final EmployeeRepository employeeRepository;
  private final AttendanceDTOMapper attendanceDTOMapper;

  public Page<AttendanceDTO> getAll(LocalDate startDate, LocalDate endDate, Pageable pageable) {
    var employees = employeeRepository.findAll();
    List<EmployeeDTO> employeeDTOS = new ArrayList<>();

    for(Employee employee : employees) {
      EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employee.getEmployeeId());
      employeeDTOS.add(employeeDTO);
    }

    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();

    for (EmployeeDTO employee : employeeDTOS) {
      List<Attendance> attendances = attendanceRepository.getAttendanceByDateBetweenAndEmployee_EmployeeId(
        startDate, endDate, employee.getEmployeeId()
      );

      Map<LocalDate, Attendance> attendanceMap = attendances.stream()
        .collect(Collectors.toMap(Attendance::getDate, Function.identity()));

      List<LocalDate> datesInRange = startDate.datesUntil(endDate.plusDays(1)).toList();

      for (LocalDate date : datesInRange) {
        Attendance attendance = attendanceMap.get(date);
        if (attendance != null) {
          attendanceDTOS.add(attendanceDTOMapper.toDTO(attendance));
        } else {
          attendanceDTOS.add(createDefaultAttendanceDTO(employee, date));
        }
      }
    }
    attendanceDTOS.sort(
      Comparator.comparing(AttendanceDTO::getAttendanceDate)
        .thenComparing(AttendanceDTO::getAttendanceStatus)
    );
    int total = attendanceDTOS.size();
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), total);

    return new PageImpl<>(
      attendanceDTOS.subList(start, end),
      pageable,
      total
    );
  }
  public Page<AttendanceDTO> getAttendanceByEmployeeId(Integer employeeId,Pageable pageale){
    Page<Attendance> attendances = attendanceRepository.getAttendanceByEmployee_EmployeeId(employeeId,pageale);
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public Page<AttendanceDTO> filterByMonth(Pageable pageable,Integer month,Integer year){
    Page<Attendance> attendances = attendanceRepository.findAttendancesByMonthAndYear(pageable,month,year);
    return  attendances.map(attendanceDTOMapper::toDTO);
  }

  public Page<AttendanceDTO> filterByStatus(Pageable pageable, String status) {
    Page<Attendance> attendances = "".equals(status)
            ? attendanceRepository.findAll(pageable)
            : attendanceRepository.findAttendancesByStatus(pageable, status);
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public AttendanceDTO getAttendanceByEmployeeIdAndDate(Integer employeeId, LocalDate date) {
    Employee employee = Optional.ofNullable(employeeRepository.getEmployeeByEmployeeId(employeeId))
      .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
    EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employee.getEmployeeId());
    Optional<Attendance> attendanceOpt = attendanceRepository.findByDateAndEmployee_EmployeeId(date, employeeId);

    return attendanceOpt
      .map(attendanceDTOMapper::toDTO)
      .orElseGet(() -> createDefaultAttendanceDTO(employeeDTO, date));
  }

  public List<AttendanceDTO> getAttendancesByEmployeeIdAndDate(Integer employeeId, LocalDate date) {
    var attendances = attendanceRepository.getAttendanceByDateBetweenAndEmployee_EmployeeId(date.withDayOfMonth(1), date.withDayOfMonth(28), employeeId);
    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();

    for(Attendance attendance : attendances) {
      attendanceDTOS.add(attendanceDTOMapper.toDTO(attendance));
    }
    return attendanceDTOS;
  }

  public void saveAttendance(AttendanceDTOList attendanceDTOList) {
    for (AttendanceDTO dto : attendanceDTOList.getAttendances()) {
      LocalTime checkIn = parseTime(String.valueOf(dto.getAttendanceCheckIn()));
      LocalTime checkOut = parseTime(String.valueOf(dto.getAttendanceCheckOut()));

      if (checkIn.isAfter(checkOut)) {
        throw new IllegalArgumentException("Check-in time cannot be after check-out time");
      }

      Employee employee = employeeRepository.findEmployeeByEmployeeId(dto.getEmployeeId());

      Attendance attendance = attendanceRepository
        .findByDateAndEmployee_EmployeeId(dto.getAttendanceDate(), employee.getEmployeeId())
        .orElse(new Attendance());

      attendance.setEmployee(employee);
      attendance.setDate(dto.getAttendanceDate());
      attendance.setCheckIn(dto.getAttendanceCheckIn());
      attendance.setCheckOut(dto.getAttendanceCheckOut());
      attendance.setStatus(dto.getAttendanceStatus());
      attendance.setOvertimeHours(dto.getAttendanceOvertimeHours());

      attendanceRepository.save(attendance);
    }
  }

  private LocalTime parseTime(String time) {
    if (time == null || time.isEmpty()) return null;
    return LocalTime.parse(time);
  }

  private AttendanceDTO createDefaultAttendanceDTO(EmployeeDTO employee, LocalDate date) {
    return AttendanceDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeFirstName(employee.getEmployeeFirstName())
      .employeeLastName(employee.getEmployeeLastName())
      .employeeCode(employee.getEmployeeCode())
      .attendanceDate(date)
      .attendanceStatus("CHƯA CHẤM CÔNG")
      .attendanceCheckIn(null)
      .attendanceCheckOut(null)
      .attendanceOvertimeHours(0.0)
      .build();
  }


  public AttendanceCountDTO countAvailableAttendance(String date) {
    int totalEmployee = employeeService.countEmployees();
    AttendanceCountDTO attendancecountDTO = AttendanceCountDTO
            .builder().totalEmployee(totalEmployee).lateEmployee(0).workedEmployee(0).absenceEmployee(0).build();
    LocalDate dates = LocalDate.parse(date);
    List<AttendanceDTO> attendanceCountDTOList = attendanceRepository.findAttendancesByDate(dates).stream().
            map(attendanceDTOMapper::toDTO).collect(Collectors.toList());
    for (AttendanceDTO dto : attendanceCountDTOList) {
      if(dto.getAttendanceStatus().equals("Đi muộn")) {
        attendancecountDTO.setLateEmployee(attendancecountDTO.getLateEmployee() + 1);
      } else {
        attendancecountDTO.setWorkedEmployee(attendancecountDTO.getWorkedEmployee() + 1);
      }
    }
    attendancecountDTO.setAbsenceEmployee(attendancecountDTO.getTotalEmployee() -
            attendancecountDTO.getWorkedEmployee() -
            attendancecountDTO.getLateEmployee());
    return attendancecountDTO;
  }
}
