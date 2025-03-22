package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.*;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.AttendanceDTOMapper;
import com.se1873.js.springboot.project.mapper.ChannelDTOMapper;
import com.se1873.js.springboot.project.mapper.DepartmentDTOMapper;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.channel.ChannelService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AttendanceService {
  private final ChannelDTOMapper channelDTOMapper;
  private final DepartmentDTOMapper departmentDTOMapper;

  private final EmployeeService employeeService;
  private final AttendanceRepository attendanceRepository;
  private final EmployeeRepository employeeRepository;
  private final AttendanceDTOMapper attendanceDTOMapper;
  private final DepartmentService departmentService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final UserRepository userRepository;
  private final MessageService messageService;
  private final ChannelService channelService;

  public Page<AttendanceDTO> getAll(LocalDate startDate, LocalDate endDate, Pageable pageable) {
    var employees = employeeService.getAll(PageRequest.of(0, 1000));
    List<EmployeeDTO> employeeDTOS = employees.getContent();
    log.info(employeeDTOS.toString());
    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();

    for (EmployeeDTO employee : employeeDTOS) {
      List<Attendance> attendances = attendanceRepository.getAttendanceByDateBetweenAndEmployee_EmployeeIdAndEmployee_IsDeleted(startDate, endDate, employee.getEmployeeId(), false);

      Map<LocalDate, Attendance> attendanceMap = attendances.stream().collect(Collectors.toMap(Attendance::getDate, Function.identity()));

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
    attendanceDTOS.sort(Comparator.comparing(AttendanceDTO::getAttendanceDate).thenComparing(AttendanceDTO::getAttendanceStatus));
    int total = attendanceDTOS.size();
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), total);

    return new PageImpl<>(attendanceDTOS.subList(start, end), pageable, total);
  }


  public Page<AttendanceDTO> getAttendanceByEmployeeId(Integer employeeId, Pageable pageale) {
    Page<Attendance> attendances = attendanceRepository.getAttendanceByEmployee_EmployeeId(employeeId, pageale);
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public Page<AttendanceDTO> filterByMonth(Pageable pageable, Integer month, Integer year, Integer employeeId) {
    Page<Attendance> attendances = attendanceRepository.findAttendancesByEmployeeAndMonthAndYear(pageable, month, year, employeeId);
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public Page<AttendanceDTO> filterByStatusAndEmployeeId(Pageable pageable, String status, Integer employeeId) {
    Page<Attendance> attendances = "".equals(status) ? attendanceRepository.findAll(pageable) : attendanceRepository.findAttendancesByEmployeeIdAndStatus(pageable, employeeId, status);
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public Page<AttendanceDTO> filterByStatus(LocalDate startDate, LocalDate endDate, Pageable pageable, String status) {
    Page<Attendance> attendances = "".equals(status) ? attendanceRepository.findAll(pageable) : attendanceRepository.findAttendancesByStatusAndDateBetween(status, startDate, endDate, pageable);
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public List<AttendanceDTO> getAllAttendances() {
    return attendanceRepository.findAll().stream().map(attendanceDTOMapper::toDTO).toList();
  }

  public Map<String, Integer> getQuantity() {
    int countOntime = 0;
    int countLate = 0;
    int countAbsent = 0;
    List<Attendance> getAll = attendanceRepository.findAll();
    for (Attendance attendance : getAll) {
      if ("Đúng giờ".equals(attendance.getStatus())) {
        countOntime++;
      } else if ("Đi muộn".equals(attendance.getStatus())) {
        countLate++;
      } else if ("Vắng mặt".equals(attendance.getStatus())) {
        countAbsent++;
      }
    }
    Map<String, Integer> result = new HashMap<>();
    result.put("Đúng giờ", countOntime);
    result.put("Đi muộn", countLate);
    result.put("Vắng mặt", countAbsent);

    return result;
  }

  public Map<String, Integer> getQuantityEmployeeDetail(Integer employeeId) {
    int countOntime = 0;
    int countLate = 0;
    int countAbsent = 0;
    List<Attendance> getAll = attendanceRepository.findAllByEmployee_EmployeeId(employeeId);
    for (Attendance attendance : getAll) {
      if ("Đúng giờ".equals(attendance.getStatus())) {
        countOntime++;
      } else if ("Đi muộn".equals(attendance.getStatus())) {
        countLate++;
      } else if ("Vắng mặt".equals(attendance.getStatus())) {
        countAbsent++;
      }
    }
    Map<String, Integer> result = new HashMap<>();
    result.put("Đúng giờ", countOntime);
    result.put("Đi muộn", countLate);
    result.put("Vắng mặt", countAbsent);

    return result;
  }

  public AttendanceDTO getAttendanceByEmployeeIdAndDate(Integer employeeId, LocalDate date) {
    Employee employee = Optional.ofNullable(employeeRepository.getEmployeeByEmployeeId(employeeId)).orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
    EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employee.getEmployeeId());
    Optional<Attendance> attendanceOpt = attendanceRepository.findByDateAndEmployee_EmployeeId(date, employeeId);

    return attendanceOpt.map(attendanceDTOMapper::toDTO).orElseGet(() -> createDefaultAttendanceDTO(employeeDTO, date));
  }

  public List<AttendanceDTO> getAttendancesByEmployeeIdAndDate(Integer employeeId, LocalDate date) {
    var attendances = attendanceRepository.getAttendanceByDateBetweenAndEmployee_EmployeeIdAndEmployee_IsDeleted(date.withDayOfMonth(1), date.withDayOfMonth(28), employeeId, false);
    List<AttendanceDTO> attendanceDTOS = new ArrayList<>();

    for (Attendance attendance : attendances) {
      attendanceDTOS.add(attendanceDTOMapper.toDTO(attendance));
    }
    return attendanceDTOS;
  }

  public List<AttendanceDTO> getAttendancesOfEmployeeIdByMonthAndYear(Integer employeeId, Integer month, Integer year) {
    return attendanceRepository.getEmployeeAttendancesByMonthAndYear(employeeId, month, year)
      .stream().map(attendanceDTOMapper::toDTO).collect(Collectors.toList());
  }

  public Integer countAttendanceByStatus(List<AttendanceDTO> attendances, String status) {
    return (int) attendances.stream()
      .filter(a -> a != null)
      .filter(attendance -> status.equals(attendance.getAttendanceStatus()))
      .count();
  }

  public Double countOvertimeHours(List<AttendanceDTO> attendances) {
    return attendances.stream()
          .filter(a -> a != null)
          .mapToDouble(a -> a.getAttendanceOvertimeHours() != null ?
            a.getAttendanceOvertimeHours() : 0.0)
          .sum();
  }

  public void updateAttendanceRecord(AttendanceDTO dto) {
    LocalTime checkIn = parseTime(String.valueOf(dto.getAttendanceCheckIn()));
    LocalTime checkOut = parseTime(String.valueOf(dto.getAttendanceCheckOut()));

    if (checkIn.isAfter(checkOut)) {
      throw new IllegalArgumentException("Check-in time cannot be after check-out time");
    }

    Attendance attendance = attendanceRepository.findByAttendanceId(dto.getAttendanceId());

    attendance.setDate(dto.getAttendanceDate());
    attendance.setCheckIn(dto.getAttendanceCheckIn());
    attendance.setCheckOut(dto.getAttendanceCheckOut());
    attendance.setStatus(dto.getAttendanceStatus());
    attendance.setOvertimeHours(dto.getAttendanceOvertimeHours());

    attendanceRepository.save(attendance);
  }

  public void saveAttendance(AttendanceDTOList attendanceDTOList) {
    for (AttendanceDTO dto : attendanceDTOList.getAttendances()) {
      LocalTime checkIn = parseTime(String.valueOf(dto.getAttendanceCheckIn()));
      LocalTime checkOut = parseTime(String.valueOf(dto.getAttendanceCheckOut()));

      if (checkIn.isAfter(checkOut)) {
        throw new IllegalArgumentException("Check-in time cannot be after check-out time");
      }

      Employee employee = employeeRepository.findEmployeeByEmployeeId(dto.getEmployeeId());

      Attendance attendance = attendanceRepository.findByDateAndEmployee_EmployeeId(dto.getAttendanceDate(), employee.getEmployeeId()).orElse(new Attendance());

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
    return AttendanceDTO.builder().employeeId(employee.getEmployeeId()).employeeFirstName(employee.getEmployeeFirstName()).employeeLastName(employee.getEmployeeLastName()).employeeCode(employee.getEmployeeCode()).attendanceDate(date).attendanceStatus("CHƯA CHẤM CÔNG").attendanceCheckIn(null).attendanceCheckOut(null).attendanceOvertimeHours(0.0).build();
  }

  public AttendanceCountDTO countAvailableAttendance(String date) {
    int totalEmployee = employeeService.countEmployees();
    AttendanceCountDTO attendancecountDTO = AttendanceCountDTO.builder().totalEmployee(totalEmployee).lateEmployee(0).workedEmployee(0).absenceEmployee(0).build();
    LocalDate dates = LocalDate.parse(date);
    List<AttendanceDTO> attendanceCountDTOList = attendanceRepository.findAttendancesByDate(dates).stream().map(attendanceDTOMapper::toDTO).collect(Collectors.toList());
    for (AttendanceDTO dto : attendanceCountDTOList) {
      if (dto.getAttendanceStatus().equals("Đi muộn")) {
        attendancecountDTO.setLateEmployee(attendancecountDTO.getLateEmployee() + 1);
      } else {
        attendancecountDTO.setWorkedEmployee(attendancecountDTO.getWorkedEmployee() + 1);
      }
    }
    attendancecountDTO.setAbsenceEmployee(attendancecountDTO.getTotalEmployee() - attendancecountDTO.getWorkedEmployee() - attendancecountDTO.getLateEmployee());
    return attendancecountDTO;
  }

  public Page<AttendanceDTO> searchAttendanceByEmployeeName(LocalDate startDate, LocalDate endDate, String employeeName, Pageable pageable) {
    if (employeeName == null || employeeName.trim().isEmpty()) {
      return Page.empty(pageable);
    }
    String normalizedSearchText = employeeName.trim();

    Page<AttendanceDTO> attendancesPage = attendanceRepository.searchAttendanceByEmployeeName(normalizedSearchText, pageable).map(attendanceDTOMapper::toDTO);
    log.info(attendancesPage.getContent().toString());
    if (startDate != null && endDate != null) {
      List<AttendanceDTO> filteredContent = attendancesPage.getContent().stream().filter(a -> {
        return a.getAttendanceDate().isAfter(startDate);
      }).collect(Collectors.toList());

      log.info(filteredContent.toString());

      Page<AttendanceDTO> filteredPage = new PageImpl<>(filteredContent, attendancesPage.getPageable(), filteredContent.size());

      log.info("Found {} attendance records matching search text: '{}' in date range {} to {}", filteredContent.size(), normalizedSearchText, startDate, endDate);

      return filteredPage;
    }

    log.info("Found {} attendance records matching search text: '{}'", attendancesPage.getTotalElements(), normalizedSearchText);

    return attendancesPage;
  }

  private List<AttendanceDTO> getAllAttendancesBetweenDates(LocalDate start, LocalDate end) {
    return attendanceRepository.findAllByDateBetween(start, end).stream().map(attendanceDTOMapper::toDTO).collect(Collectors.toList());
  }

  public AttendanceAnalyticDTO.WeeklyTrendsDTO getWeeklyTrends(Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
    LocalDate start, end;

    if (startDate != null && endDate != null) {
      start = startDate;
      end = endDate;
    } else if (year != null && month != null) {
      YearMonth yearMonth = YearMonth.of(year, month);
      start = yearMonth.atDay(1);
      end = yearMonth.atEndOfMonth();
    } else {
      LocalDate today = LocalDate.now();
      start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    List<AttendanceDTO> attendances = getAllAttendancesBetweenDates(start, end);

    long totalEmployees = employeeRepository.count();

    Map<DayOfWeek, Map<String, Long>> attendanceStatusByDay = new HashMap<>();

    for (DayOfWeek day : DayOfWeek.values()) {
      Map<String, Long> statusCounts = new HashMap<>();
      statusCounts.put("On Time", 0L);
      statusCounts.put("Late", 0L);
      statusCounts.put("Absent", 0L);
      attendanceStatusByDay.put(day, statusCounts);
    }

    for (AttendanceDTO attendance : attendances) {
      DayOfWeek dayOfWeek = attendance.getAttendanceDate().getDayOfWeek();
      String status = attendance.getAttendanceStatus();

      String mappedStatus;
      if ("Đúng giờ".equals(status) || "On Time".equalsIgnoreCase(status)) {
        mappedStatus = "On Time";
      } else if ("Đi muộn".equals(status) || "Late".equalsIgnoreCase(status)) {
        mappedStatus = "Late";
      } else {
        mappedStatus = "Absent";
      }

      Map<String, Long> dayCounts = attendanceStatusByDay.get(dayOfWeek);
      dayCounts.put(mappedStatus, dayCounts.get(mappedStatus) + 1);
    }

    long workingDays = 0;
    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
        workingDays++;
      }
    }

    for (DayOfWeek day : DayOfWeek.values()) {
      Map<String, Long> dayCounts = attendanceStatusByDay.get(day);
      long recordedAttendances = dayCounts.get("On Time") + dayCounts.get("Late");

      if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
        dayCounts.put("Absent", totalEmployees - recordedAttendances);
      } else {
        dayCounts.put("Absent", 0L);
      }
    }

    List<String> dayLabels = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

    List<Double> onTimePercentages = new ArrayList<>();
    List<Double> latePercentages = new ArrayList<>();
    List<Double> absentPercentages = new ArrayList<>();

    for (DayOfWeek day : DayOfWeek.values()) {
      Map<String, Long> dayCounts = attendanceStatusByDay.get(day);

      if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && totalEmployees > 0) {
        onTimePercentages.add(Math.round(dayCounts.get("On Time") * 100.0 / totalEmployees * 10) / 10.0);
        latePercentages.add(Math.round(dayCounts.get("Late") * 100.0 / totalEmployees * 10) / 10.0);
        absentPercentages.add(Math.round(dayCounts.get("Absent") * 100.0 / totalEmployees * 10) / 10.0);
      } else {
        onTimePercentages.add(0.0);
        latePercentages.add(0.0);
        absentPercentages.add(0.0);
      }
    }

    List<AttendanceAnalyticDTO.SeriesDTO> series = List.of(AttendanceAnalyticDTO.SeriesDTO.builder().name("On Time %").color("#10B981").data(onTimePercentages).build(), AttendanceAnalyticDTO.SeriesDTO.builder().name("Late %").color("#F59E0B").data(latePercentages).build(), AttendanceAnalyticDTO.SeriesDTO.builder().name("Absent %").color("#EF4444").data(absentPercentages).build());

    double weeklyAverage = 0;
    int workingDayCount = 0;
    for (int i = 0; i < 5; i++) {
      weeklyAverage += onTimePercentages.get(i);
      workingDayCount++;
    }
    weeklyAverage = workingDayCount > 0 ? weeklyAverage / workingDayCount : 0;

    int bestDayIndex = 0;
    double bestRate = 0;
    for (int i = 0; i < 5; i++) {
      if (onTimePercentages.get(i) > bestRate) {
        bestRate = onTimePercentages.get(i);
        bestDayIndex = i;
      }
    }

    AttendanceAnalyticDTO.BestDayDTO bestDay = AttendanceAnalyticDTO.BestDayDTO.builder().name(dayLabels.get(bestDayIndex)).rate(bestRate).build();

    LocalDate previousWeekStart = start.minusWeeks(1);
    LocalDate previousWeekEnd = end.minusWeeks(1);

    List<AttendanceDTO> previousWeekAttendances = getAllAttendancesBetweenDates(previousWeekStart, previousWeekEnd);

    long previousWeekOnTimeCount = previousWeekAttendances.stream().filter(att -> "Đúng giờ".equals(att.getAttendanceStatus()) || "On Time".equalsIgnoreCase(att.getAttendanceStatus())).count();

    long currentWeekOnTimeCount = attendances.stream().filter(att -> "Đúng giờ".equals(att.getAttendanceStatus()) || "On Time".equalsIgnoreCase(att.getAttendanceStatus())).count();

    double previousWeekOnTimePercentage = totalEmployees > 0 ? previousWeekOnTimeCount * 100.0 / (totalEmployees * workingDays) : 0;
    double currentWeekOnTimePercentage = totalEmployees > 0 ? currentWeekOnTimeCount * 100.0 / (totalEmployees * workingDays) : 0;
    double improvement = currentWeekOnTimePercentage - previousWeekOnTimePercentage;

    AttendanceAnalyticDTO.SummaryDTO summary = AttendanceAnalyticDTO.SummaryDTO.builder().weeklyAverage(Math.round(weeklyAverage * 10) / 10.0).bestDay(bestDay).improvement(Math.round(improvement * 10) / 10.0).build();

    return AttendanceAnalyticDTO.WeeklyTrendsDTO.builder().categories(dayLabels).series(series).summary(summary).build();
  }

  public AttendanceAnalyticDTO.MonthlyTrendsDTO getMonthlyTrends(Integer year, LocalDate startDate, LocalDate endDate) {
    LocalDate start, end;
    int targetYear;

    if (startDate != null && endDate != null) {
      start = startDate;
      end = endDate;
      targetYear = start.getYear();
    } else {
      targetYear = year != null ? year : LocalDate.now().getYear();
      start = LocalDate.of(targetYear, 1, 1);
      end = LocalDate.of(targetYear, 12, 31);
    }

    List<AttendanceDTO> attendances = getAllAttendancesBetweenDates(start, end);

    List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    long totalEmployees = employeeRepository.count();

    Map<Month, Map<String, Long>> attendanceStatusByMonth = new HashMap<>();

    for (Month month : Month.values()) {
      Map<String, Long> statusCounts = new HashMap<>();
      statusCounts.put("On Time", 0L);
      statusCounts.put("Late", 0L);
      statusCounts.put("Absent", 0L);
      attendanceStatusByMonth.put(month, statusCounts);
    }

    for (AttendanceDTO attendance : attendances) {
      Month month = attendance.getAttendanceDate().getMonth();
      String status = attendance.getAttendanceStatus();

      String mappedStatus;
      if ("Đúng giờ".equals(status) || "On Time".equalsIgnoreCase(status)) {
        mappedStatus = "On Time";
      } else if ("Đi muộn".equals(status) || "Late".equalsIgnoreCase(status)) {
        mappedStatus = "Late";
      } else {
        mappedStatus = "Absent";
      }

      Map<String, Long> monthCounts = attendanceStatusByMonth.get(month);
      monthCounts.put(mappedStatus, monthCounts.get(mappedStatus) + 1);
    }

    Map<Month, Integer> workingDaysPerMonth = new HashMap<>();
    for (Month month : Month.values()) {
      YearMonth yearMonth = YearMonth.of(targetYear, month);
      int workingDays = 0;

      for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
        LocalDate date = yearMonth.atDay(day);
        if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
          workingDays++;
        }
      }

      workingDaysPerMonth.put(month, workingDays);
    }

    for (Month month : Month.values()) {
      Map<String, Long> monthCounts = attendanceStatusByMonth.get(month);
      int workingDays = workingDaysPerMonth.get(month);
      long expectedAttendances = totalEmployees * workingDays;
      long recordedAttendances = monthCounts.get("On Time") + monthCounts.get("Late");

      monthCounts.put("Absent", expectedAttendances - recordedAttendances);
    }

    List<Double> onTimePercentages = new ArrayList<>();
    List<Double> latePercentages = new ArrayList<>();
    List<Double> absentPercentages = new ArrayList<>();

    for (Month month : Month.values()) {
      Map<String, Long> monthCounts = attendanceStatusByMonth.get(month);
      int workingDays = workingDaysPerMonth.get(month);
      long expectedAttendances = totalEmployees * workingDays;

      if (expectedAttendances > 0) {
        onTimePercentages.add(Math.round(monthCounts.get("On Time") * 100.0 / expectedAttendances * 10) / 10.0);
        latePercentages.add(Math.round(monthCounts.get("Late") * 100.0 / expectedAttendances * 10) / 10.0);
        absentPercentages.add(Math.round(monthCounts.get("Absent") * 100.0 / expectedAttendances * 10) / 10.0);
      } else {
        onTimePercentages.add(0.0);
        latePercentages.add(0.0);
        absentPercentages.add(0.0);
      }
    }

    List<AttendanceAnalyticDTO.SeriesDTO> series = List.of(AttendanceAnalyticDTO.SeriesDTO.builder().name("On Time %").color("#10B981").data(onTimePercentages).build(), AttendanceAnalyticDTO.SeriesDTO.builder().name("Late %").color("#F59E0B").data(latePercentages).build(), AttendanceAnalyticDTO.SeriesDTO.builder().name("Absent %").color("#EF4444").data(absentPercentages).build());

    double monthlyAverage = onTimePercentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);

    int bestMonthIndex = 0;
    double bestRate = 0;
    for (int i = 0; i < onTimePercentages.size(); i++) {
      if (onTimePercentages.get(i) > bestRate) {
        bestRate = onTimePercentages.get(i);
        bestMonthIndex = i;
      }
    }

    AttendanceAnalyticDTO.BestMonthDTO bestMonth = AttendanceAnalyticDTO.BestMonthDTO.builder().name(months.get(bestMonthIndex)).rate(bestRate).build();

    int previousYear = targetYear - 1;
    LocalDate previousYearStart = LocalDate.of(previousYear, 1, 1);
    LocalDate previousYearEnd = LocalDate.of(previousYear, 12, 31);

    List<AttendanceDTO> previousYearAttendances = getAllAttendancesBetweenDates(previousYearStart, previousYearEnd);

    long previousYearOnTimeCount = previousYearAttendances.stream().filter(att -> "Đúng giờ".equals(att.getAttendanceStatus()) || "On Time".equalsIgnoreCase(att.getAttendanceStatus())).count();

    long currentYearOnTimeCount = attendances.stream().filter(att -> "Đúng giờ".equals(att.getAttendanceStatus()) || "On Time".equalsIgnoreCase(att.getAttendanceStatus())).count();

    int previousYearWorkingDays = workingDaysPerMonth.values().stream().mapToInt(Integer::intValue).sum();
    int currentYearWorkingDays = workingDaysPerMonth.values().stream().mapToInt(Integer::intValue).sum();

    double previousYearOnTimePercentage = previousYearWorkingDays > 0 ? previousYearOnTimeCount * 100.0 / (totalEmployees * previousYearWorkingDays) : 0;
    double currentYearOnTimePercentage = currentYearWorkingDays > 0 ? currentYearOnTimeCount * 100.0 / (totalEmployees * currentYearWorkingDays) : 0;
    double yearOverYearChange = currentYearOnTimePercentage - previousYearOnTimePercentage;

    AttendanceAnalyticDTO.SummaryDTO summary = AttendanceAnalyticDTO.SummaryDTO.builder().monthlyAverage(Math.round(monthlyAverage * 10) / 10.0).bestMonth(bestMonth).yearOverYearChange(Math.round(yearOverYearChange * 10) / 10.0).build();

    return AttendanceAnalyticDTO.MonthlyTrendsDTO.builder().categories(months).series(series).summary(summary).build();
  }

  public AttendanceAnalyticDTO.DepartmentComparisonDTO getDepartmentComparison(Integer year, Integer month, LocalDate date) {
    LocalDate start, end;

    if (date != null) {
      start = date;
      end = date;
    } else if (year != null && month != null) {
      YearMonth yearMonth = YearMonth.of(year, month);
      start = yearMonth.atDay(1);
      end = yearMonth.atEndOfMonth();
    } else {
      YearMonth currentMonth = YearMonth.now();
      start = currentMonth.atDay(1);
      end = currentMonth.atEndOfMonth();
    }

    List<AttendanceDTO> attendances = getAllAttendancesBetweenDates(start, end);
    List<DepartmentDTO> departments = departmentService.getAllDepartments().stream().map(departmentDTOMapper::toDTO).collect(Collectors.toList());

    int workingDays = 0;
    for (LocalDate currentDate = start; !currentDate.isAfter(end); currentDate = currentDate.plusDays(1)) {
      if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
        workingDays++;
      }
    }

    List<AttendanceAnalyticDTO.DepartmentDataDTO> departmentData = new ArrayList<>();
    Map<Integer, String> departmentColors = Map.of(1, "#8B5CF6", 2, "#EC4899", 3, "#10B981", 4, "#3B82F6", 5, "#F59E0B", 6, "#EF4444", 7, "#6366F1", 8, "#14B8A6", 9, "#F97316", 10, "#8B5CF6");

    Map<Integer, Double> departmentImprovement = new HashMap<>();

    for (DepartmentDTO department : departments) {
      int departmentId = department.getDepartmentId();

      long departmentEmployeeCount = employeeService.getEmployeesByDepartmentId(departmentId, PageRequest.of(0, 1000)).getContent().size();

      if (departmentEmployeeCount == 0) {
        continue;
      }

      long expectedAttendances = departmentEmployeeCount * workingDays;

      List<AttendanceDTO> departmentAttendances = attendances.stream().filter(att -> att.getDepartmentId() != null && att.getDepartmentId() == departmentId).collect(Collectors.toList());

      long onTimeCount = departmentAttendances.stream().filter(att -> "Đúng giờ".equals(att.getAttendanceStatus()) || "On Time".equalsIgnoreCase(att.getAttendanceStatus())).count();

      double onTimePercentage = expectedAttendances > 0 ? onTimeCount * 100.0 / expectedAttendances : 0;

      LocalDate previousPeriodStart = start.minusMonths(1);
      LocalDate previousPeriodEnd = end.minusMonths(1);

      int previousWorkingDays = 0;
      for (LocalDate currentDate = previousPeriodStart; !currentDate.isAfter(previousPeriodEnd); currentDate = currentDate.plusDays(1)) {
        if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
          previousWorkingDays++;
        }
      }

      List<AttendanceDTO> previousPeriodAttendances = getAllAttendancesBetweenDates(previousPeriodStart, previousPeriodEnd).stream().filter(att -> att.getDepartmentId() != null && att.getDepartmentId() == departmentId).collect(Collectors.toList());

      long previousOnTimeCount = previousPeriodAttendances.stream().filter(att -> "Đúng giờ".equals(att.getAttendanceStatus()) || "On Time".equalsIgnoreCase(att.getAttendanceStatus())).count();

      long previousExpectedAttendances = departmentEmployeeCount * previousWorkingDays;

      double previousOnTimePercentage = previousExpectedAttendances > 0 ? previousOnTimeCount * 100.0 / previousExpectedAttendances : 0;

      double improvement = onTimePercentage - previousOnTimePercentage;
      departmentImprovement.put(departmentId, improvement);

      String color = departmentColors.getOrDefault(departmentId, "#8B5CF6");

      departmentData.add(AttendanceAnalyticDTO.DepartmentDataDTO.builder().name(department.getDepartmentName()).value(Math.round(onTimePercentage * 10) / 10.0).color(color).build());
    }

    departmentData.sort((d1, d2) -> Double.compare(d2.getValue(), d1.getValue()));

    AttendanceAnalyticDTO.DepartmentInfoDTO topDepartment = null;
    if (!departmentData.isEmpty()) {
      AttendanceAnalyticDTO.DepartmentDataDTO top = departmentData.get(0);
      topDepartment = AttendanceAnalyticDTO.DepartmentInfoDTO.builder().name(top.getName()).rate(top.getValue()).build();
    }

    AttendanceAnalyticDTO.ImprovedDepartmentDTO mostImproved = null;
    if (!departmentImprovement.isEmpty()) {
      Map.Entry<Integer, Double> mostImprovedEntry = departmentImprovement.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);

      if (mostImprovedEntry != null) {
        int departmentId = mostImprovedEntry.getKey();
        String departmentName = departments.stream().filter(d -> d.getDepartmentId() == departmentId).findFirst().map(DepartmentDTO::getDepartmentName).orElse("Unknown");

        mostImproved = AttendanceAnalyticDTO.ImprovedDepartmentDTO.builder().name(departmentName).improvement(Math.round(mostImprovedEntry.getValue() * 10) / 10.0).build();
      }
    }

    AttendanceAnalyticDTO.DepartmentInfoDTO needsAttention = null;
    if (departmentData.size() > 1) {
      AttendanceAnalyticDTO.DepartmentDataDTO worst = departmentData.get(departmentData.size() - 1);
      needsAttention = AttendanceAnalyticDTO.DepartmentInfoDTO.builder().name(worst.getName()).rate(worst.getValue()).build();
    }

    AttendanceAnalyticDTO.DepartmentSummaryDTO summary = AttendanceAnalyticDTO.DepartmentSummaryDTO.builder().topDepartment(topDepartment).mostImproved(mostImproved).needsAttention(needsAttention).build();

    return AttendanceAnalyticDTO.DepartmentComparisonDTO.builder().departments(departmentData).summary(summary).build();
  }

  public String checkAttendance(Integer employeeId, String type) {
    if ("clockin".equals(type)) {
      AttendanceDTO attendanceDTO = getAttendanceByEmployeeIdAndDate(employeeId, LocalDate.now());
      Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeId);
      if (attendanceDTO.getAttendanceCheckIn() != null) return "Already Clock In";
      Attendance attendance = Attendance.builder().checkIn(LocalTime.now()).checkOut(null).date(LocalDate.now()).status("Đúng giờ").employee(employee).build();

      saveAttendance(attendance);
      String notificationText = "Employee ID #" + employee.getEmployeeId() + " clock-in";
      ChannelDTO systemChannel = channelDTOMapper.toDTO(channelService.getChannelByChannelName("System"));

      User adminUser = userRepository.findUserByRole("SYSTEM");
      MessageDTO messageDTO = MessageDTO.builder().channelId(systemChannel.getChannelId()).userId(adminUser.getUserId()).username("System Notification").messageContent(notificationText).messageCreatedAt(LocalDateTime.now()).build();

      MessageDTO savedMessage = messageService.saveMessage(messageDTO);

      simpMessagingTemplate.convertAndSend("/topic/chat/" + systemChannel.getChannelId(), savedMessage);
      return "Clock In";
    } else if ("clockout".equals(type)) {
      AttendanceDTO attendance = getAttendanceByEmployeeIdAndDate(employeeId, LocalDate.now());
      log.info(attendance.toString());
      if (attendance.getAttendanceCheckIn() == null) return "Not yet Clock In";
      if (attendance.getAttendanceCheckOut() != null) return "Already Clock Out";
      attendance.setAttendanceCheckOut(LocalTime.now());
      attendance.setAttendanceOvertimeHours(calculateOvertimeHours(attendance.getAttendanceCheckIn(), attendance.getAttendanceCheckOut()));

      Attendance existedAttendance = attendanceRepository.findByAttendanceId(attendance.getAttendanceId());
      existedAttendance.setCheckOut(attendance.getAttendanceCheckOut());
      existedAttendance.setOvertimeHours(attendance.getAttendanceOvertimeHours());

      saveAttendance(existedAttendance);
      String notificationText = "Employee ID #" + attendance.getEmployeeId() + " clock-out";
      ChannelDTO systemChannel = channelDTOMapper.toDTO(channelService.getChannelByChannelName("System"));

      User adminUser = userRepository.findUserByRole("SYSTEM");
      MessageDTO messageDTO = MessageDTO.builder().channelId(systemChannel.getChannelId()).userId(adminUser.getUserId()).username("System Notification").messageContent(notificationText).messageCreatedAt(LocalDateTime.now()).build();

      MessageDTO savedMessage = messageService.saveMessage(messageDTO);

      simpMessagingTemplate.convertAndSend("/topic/chat/" + systemChannel.getChannelId(), savedMessage);
      return "Clock Out";
    }
    return null;
  }

  private Double calculateOvertimeHours(LocalTime checkIn, LocalTime checkOut) {
    if (checkIn == null || checkOut == null) {
      return 0.0;
    }

    try {
      LocalTime standardEnd = LocalTime.of(18, 0);

      double overtimeHours = 0.0;

      if (checkOut.isAfter(standardEnd)) {
        long overtimeMinutes = ChronoUnit.MINUTES.between(standardEnd, checkOut);
        overtimeHours = Math.round(overtimeMinutes / 6.0) / 10.0;
      }

      return overtimeHours;
    } catch (Exception e) {
      log.error("Error calculating overtime hours: {}", e.getMessage());
      return 0.0;
    }
  }

  public void saveAttendance(Attendance attendance) {
    try {
      log.info("Saving attendance: {}", attendance);
      Attendance saved = attendanceRepository.save(attendance);
      log.info("Saved attendance with ID: {}", saved.getAttendanceId());
    } catch (Exception e) {
      log.error("Error saving attendance: ", e);
      throw e;
    }
  }

  public Resource exportAttendanceToExcel(List<Integer> employeeIds, LocalDate startDate, LocalDate endDate) {
    Pageable pageable = PageRequest.of(0, 1000);
    List<Attendance> attendances;

    if (employeeIds != null && !employeeIds.isEmpty()) {
      attendances = attendanceRepository.findAllByEmployee_EmployeeIdIn(employeeIds);
    } else {
      attendances = attendanceRepository.findAttendancesByDateRange(startDate, endDate);
    }


    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Attendance");

      Font titleFont = workbook.createFont();
      titleFont.setBold(true);
      titleFont.setFontHeightInPoints((short) 16);

      CellStyle titleStyle = workbook.createCellStyle();
      titleStyle.setFont(titleFont);
      titleStyle.setAlignment(HorizontalAlignment.CENTER);
      titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("Attendance Data Export");
      titleCell.setCellStyle(titleStyle);

      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));  // Merge columns for title

      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setFontHeightInPoints((short) 12);
      headerFont.setColor(IndexedColors.WHITE.getIndex());

      CellStyle headerStyle = workbook.createCellStyle();
      headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setFont(headerFont);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);
      headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
      headerStyle.setBorderBottom(BorderStyle.THIN);
      headerStyle.setBorderTop(BorderStyle.THIN);
      headerStyle.setBorderRight(BorderStyle.THIN);
      headerStyle.setBorderLeft(BorderStyle.THIN);

      CellStyle dataStyle = workbook.createCellStyle();
      dataStyle.setBorderBottom(BorderStyle.THIN);
      dataStyle.setBorderTop(BorderStyle.THIN);
      dataStyle.setBorderRight(BorderStyle.THIN);
      dataStyle.setBorderLeft(BorderStyle.THIN);

      Row headerRow = sheet.createRow(1);
      String[] columns = {"Employee ID", "Name", "Date", "Check In", "Check Out", "Status"};
      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      int rowIdx = 2;
      for (Attendance attendance : attendances) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(attendance.getEmployee().getEmployeeId());
        row.createCell(1).setCellValue(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        row.createCell(2).setCellValue(attendance.getDate().toString());
        row.createCell(3).setCellValue(attendance.getCheckIn() != null ? attendance.getCheckIn().toString() : "");
        row.createCell(4).setCellValue(attendance.getCheckOut() != null ? attendance.getCheckOut().toString() : "");
        row.createCell(5).setCellValue(attendance.getStatus());

        for (int i = 0; i < 6; i++) {
          row.getCell(i).setCellStyle(dataStyle);
        }
      }

      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (IOException e) {
      log.error("Error exporting attendance data to Excel", e);
      throw new RuntimeException("Error exporting attendance data to Excel", e);
    }
  }

  public Page<AttendanceDTO> getAll(LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
    log.info("Fetching attendance records from {} to {} with status: {}", startDate, endDate, status);

    Page<Attendance> attendances;
    if (status != null && !status.isEmpty()) {
      attendances = attendanceRepository.findAttendancesByStatusAndDateRange(startDate, endDate, status, pageable);
    } else {
      attendances = attendanceRepository.findAttendancesByDateRange(startDate, endDate, pageable);
    }

    log.info("Fetched {} attendance records", attendances.getTotalElements());
    return attendances.map(attendanceDTOMapper::toDTO);
  }

  public int countDailyAttendance(LocalDate date) {
    return attendanceRepository.countCheckedInEmployees(date);
  }


  public List<EmployeeAttendanceStatusDTO> getEmployeeAttendanceStatus(String date, Pageable pageable) {
    YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
    int year = yearMonth.getYear();
    int month = yearMonth.getMonthValue();
    int daysInMonth = yearMonth.lengthOfMonth();

    List<Attendance> attendanceList = attendanceRepository.findAllAttendanceByMonthYear(year, month);


    Map<Integer, EmployeeAttendanceStatusDTO> employeeStatusMap = new HashMap<>();

    for (Attendance attendance : attendanceList) {
      Employee employee = attendance.getEmployee();
      int employeeId = employee.getEmployeeId();


      EmployeeAttendanceStatusDTO employeeAttendanceStatusDTO = employeeStatusMap.getOrDefault(employeeId, EmployeeAttendanceStatusDTO.builder().employee(employeeService.getEmployeeByEmployeeId(employeeId)).countLateDays(0).countAbsentDays(0).monthYear(yearMonth).build());

      if (attendance.getStatus().startsWith("Đi muộn")) {
        employeeAttendanceStatusDTO.setCountLateDays(employeeAttendanceStatusDTO.getCountLateDays() + 1);
      } else if (attendance.getStatus().equals("Đúng giờ")) {
        employeeAttendanceStatusDTO.setCountAbsentDays(employeeAttendanceStatusDTO.getCountAbsentDays() + 1);
      }


      employeeStatusMap.put(employeeId, employeeAttendanceStatusDTO);
    }
    for (EmployeeAttendanceStatusDTO dto : employeeStatusMap.values()) {
      dto.setMonthYear(dto.getMonthYear());
      int totalDaysPresent = dto.getCountAbsentDays() + dto.getCountLateDays();
      dto.setCountAbsentDays(daysInMonth - countWeekendDays(year, month) - totalDaysPresent);
    }

    List<EmployeeAttendanceStatusDTO> employeeAttendanceStatusDTOS = new ArrayList<>(employeeStatusMap.values());

    return employeeAttendanceStatusDTOS;
  }

  public List<EmployeeAttendanceStatusDTO> findEmployeeAttendanceStatusbyEmployeeName(String search, Pageable pageable, String date) {
    String[] searchTerms = search.trim().split("\\s+");
    String firstName = searchTerms[0].toLowerCase();
    String lastName = searchTerms.length > 1 ? Arrays.stream(searchTerms, 1, searchTerms.length).collect(Collectors.joining(" ")).toLowerCase() : null;
    Page<Employee> employees = employeeRepository.searchEmployeebyEmployeeName(firstName, lastName, pageable);
    System.out.println("date: " + date);
    System.out.println("check: " + employees.getContent());
    List<Long> employeeIds = employees.getContent().stream().map(emp -> emp.getEmployeeId().longValue()).collect(Collectors.toList());

    System.out.println("check Employeeid: " + employeeIds.toString());


    if (employeeIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<EmployeeAttendanceStatusDTO> employeeAttendanceStatusDTOList = getEmployeeAttendanceStatus(date, pageable);
//    for (EmployeeAttendanceStatusDTO dto : employeeAttendanceStatusDTOList) {
//      System.out.println("DTO Employee ID Type: " + dto.getEmployee().getEmployeeId().getClass().getName());
//    }
//    System.out.println("Set Employee ID Type: " + employeeIds.get(0).getClass().getName());

    System.out.println(employeeAttendanceStatusDTOList);
    Set<Long> employeeIdSet = employeeIds.stream().map(Long::valueOf).collect(Collectors.toSet());

    return employeeAttendanceStatusDTOList.stream().filter(dto -> employeeIdSet.contains(dto.getEmployee().getEmployeeId().longValue())) // Ép kiểu về Long trước khi so sánh
      .collect(Collectors.toList());
  }

  public int countWeekendDays(int year, int month) {
    Calendar calendar = Calendar.getInstance();

    calendar.set(year, month - 1, 1);
    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

    int count = 0;
    for (int day = 1; day <= daysInMonth; day++) {
      calendar.set(year, month - 1, day);
      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
        count++;

      }
    }
    return count;
  }

  public List<EmployeeAttendanceStatusDTO> departmentFilter(String departmentName, Pageable pageable, String date) {
    System.out.println(departmentName);
    Page<Employee> employees = employeeRepository.findEmployeesByDepartmentName(departmentName, pageable);
    System.out.println(employees.getContent());
    List<Long> employeeIds = employees.getContent().stream().map(emp -> emp.getEmployeeId().longValue()).collect(Collectors.toList());

    System.out.println("check Employeeid: " + employeeIds.toString());


    if (employeeIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<EmployeeAttendanceStatusDTO> employeeAttendanceStatusDTOList = getEmployeeAttendanceStatus(date, pageable);

    Set<Long> employeeIdSet = employeeIds.stream().map(Long::valueOf).collect(Collectors.toSet());

    return employeeAttendanceStatusDTOList.stream().filter(dto -> employeeIdSet.contains(dto.getEmployee().getEmployeeId().longValue())).collect(Collectors.toList());
  }


}
