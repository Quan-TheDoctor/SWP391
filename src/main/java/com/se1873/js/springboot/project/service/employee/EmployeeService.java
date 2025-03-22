package com.se1873.js.springboot.project.service.employee;

import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.mapper.DepartmentDTOMapper;
import com.se1873.js.springboot.project.mapper.DependentDTOMapper;
import com.se1873.js.springboot.project.mapper.EmployeeDTOMapper;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.position.PositionService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.command.EmployeeCommandService;
import com.se1873.js.springboot.project.service.employee.query.EmployeeQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
  private final DepartmentDTOMapper departmentDTOMapper;
  private final DependentDTOMapper dependentDTOMapper;
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final ContractRepository contractRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final EmployeeDTOMapper employeeDTOMapper;
  private final EmployeeQueryService employeeQueryService;
  private final DepartmentService departmentService;
  private final PositionService positionService;
  private final EmployeeCommandService employeeCommandService;

  public Page<EmployeeDTO> getAll(Pageable pageable) {
    return employeeQueryService.getAll(pageable);
  }

  public Page<EmployeeDTO> getEmployeesByDepartmentId(Integer departmentId, Pageable pageable) {
    return employeeQueryService.getEmployeesByDepartmentId(departmentId, pageable);
  }

  public List<EmployeeDTO> getAllEmployees() {
    return employeeQueryService.getAll();
  }

  public List<EmployeeDTO> getEmployeesByDepartmentId(Integer departmentId) {
    return employeeQueryService.getEmployeesByDepartmentId(departmentId)
      .stream().map(employeeDTOMapper::toDTO)
      .filter(e -> e.getDepartmentId().equals(departmentId))
      .collect(Collectors.toList());
  }

  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return employeeQueryService.getEmployeeByEmployeeId(employeeId);
  }

  public Page<EmployeeDTO> sort(Page<EmployeeDTO> source, String direction, String field) {
    if (source == null) source = employeeQueryService.getAll(source.getPageable());
    List<EmployeeDTO> sorted = employeeQueryService.sort(source, direction, field);

    return new PageImpl<>(sorted, source.getPageable(), sorted.size());
  }

  public Page<EmployeeDTO> filterByField(String field, String value, Pageable pageable) {
    return employeeQueryService.filter(field, value, pageable);
  }

  public Page<EmployeeDTO> search(Pageable pageable, String query) {
    log.info(query);
    String searchTerm = query.trim();
    log.info(searchTerm);
    return employeeQueryService.search(searchTerm, pageable).map(employeeDTOMapper::toDTO);
  }

  public Resource exportToExcel(List<Integer> employeeIds, String departmentFilter, String positionFilter) {
    Pageable pageable = PageRequest.of(0, 1000);
    List<Employee> employees;

    if (employeeIds != null && !employeeIds.isEmpty()) {
      employees = employeeRepository.findAllByEmployeeIdIn(employeeIds);
    } else if (!"all".equals(departmentFilter) && !"all".equals(positionFilter)) {
      employees = employeeRepository.findEmployeesByDepartmentName(departmentFilter, pageable).toList()
        .stream()
        .filter(emp -> employeeRepository.findEmployeesByPositionName(positionFilter, pageable)
          .toList().stream().anyMatch(e -> e.getEmployeeId().equals(emp.getEmployeeId())))
        .toList();
    } else if (!"all".equals(departmentFilter)) {
      employees = employeeRepository.findEmployeesByDepartmentName(departmentFilter, pageable).toList();
    } else if (!"all".equals(positionFilter)) {
      employees = employeeRepository.findEmployeesByPositionName(positionFilter, pageable).toList();
    } else {
      employees = employeeRepository.findAll(pageable).getContent();
    }

    if (employees.isEmpty()) {
      log.warn("Không có nhân viên nào để export.");
    }

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Employees");

      Font titleFont = workbook.createFont();
      titleFont.setBold(true);
      titleFont.setFontHeightInPoints((short) 16);

      CellStyle titleStyle = workbook.createCellStyle();
      titleStyle.setFont(titleFont);
      titleStyle.setAlignment(HorizontalAlignment.CENTER);
      titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("Information Employee");
      titleCell.setCellStyle(titleStyle);

      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

      // Tạo font cho tiêu đề cột
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setFontHeightInPoints((short) 12);
      headerFont.setColor(IndexedColors.WHITE.getIndex());

      // Tạo style cho tiêu đề cột
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

      // Style cho dữ liệu
      CellStyle dataStyle = workbook.createCellStyle();
      dataStyle.setBorderBottom(BorderStyle.THIN);
      dataStyle.setBorderTop(BorderStyle.THIN);
      dataStyle.setBorderRight(BorderStyle.THIN);
      dataStyle.setBorderLeft(BorderStyle.THIN);

      // Style cho cột lương
      CellStyle salaryStyle = workbook.createCellStyle();
      salaryStyle.cloneStyleFrom(dataStyle);
      salaryStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

      // Tạo hàng tiêu đề cột
      Row headerRow = sheet.createRow(2);
      String[] columns = {"ID", "Name", "Department", "Position", "Salary"};
      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Ghi dữ liệu vào file Excel
      int rowIdx = 3;
      for (Employee emp : employees) {
        EmploymentHistory employmentHistory = employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(emp.getEmployeeId(), true);
        Department department = employmentHistory.getDepartment();
        Position position = employmentHistory.getPosition();
        Contract contract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(emp.getEmployeeId(), true);

        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(emp.getEmployeeId());
        row.createCell(1).setCellValue(emp.getFirstName() + " " + emp.getLastName());
        row.createCell(2).setCellValue(department.getDepartmentName());
        row.createCell(3).setCellValue(position.getPositionName());

        Cell salaryCell = row.createCell(4);
        salaryCell.setCellValue(contract.getBaseSalary());
        salaryCell.setCellStyle(salaryStyle);

        // Áp dụng style cho các ô dữ liệu
        for (int i = 0; i < 4; i++) {
          row.getCell(i).setCellStyle(dataStyle);
        }
      }

      // Tự động điều chỉnh độ rộng cột
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (IOException e) {
      log.error("Lỗi khi xuất file Excel", e);
      throw new RuntimeException("Lỗi khi xuất file Excel", e);
    }
  }

  public Page<EmployeeDTO> exportFilteredEmployees(String departmentFilter, String positionFilter, Pageable pageable) {
    Page<Employee> employees;

    if (!"all".equals(departmentFilter) && !"all".equals(positionFilter)) {
      List<Employee> departmentEmployees = employeeRepository.findEmployeesByDepartmentName(departmentFilter, pageable).toList();
      List<Employee> positionEmployees = employeeRepository.findEmployeesByPositionName(positionFilter, pageable).toList();

      List<Employee> filteredEmployees = departmentEmployees.stream()
        .filter(positionEmployees::contains)
        .toList();
      employees = new PageImpl<>(filteredEmployees, pageable, filteredEmployees.size());
    } else if (!"all".equals(departmentFilter)) {
      employees = employeeRepository.findEmployeesByDepartmentName(departmentFilter, pageable);
    } else if (!"all".equals(positionFilter)) {
      employees = employeeRepository.findEmployeesByPositionName(positionFilter, pageable);
    } else {
      employees = employeeRepository.findAll(pageable);
    }

    return employees.map(employeeDTOMapper::toDTO);
  }

  public void saveEmployee(EmployeeDTO employeeDTO) {
    Department department = departmentService.findDepartmentByDepartmentId(employeeDTO.getDepartmentId());
    Position position = positionService.findPositionByPositionId(employeeDTO.getPositionId());

    employeeCommandService.saveEmployee(employeeDTO, department, position);
  }

  public int countEmployees() {
    return employeeQueryService.getEmployeeCount();
  }
  public EmployeeCountDTO getAvailableAndUnavailableEmployeeCount() {
    return employeeQueryService.getAvailableAndUnavailableEmployeeCount();
  }

  public Double getAverageSalary(List<EmployeeDTO> employees) {
    return employeeQueryService.getAverageSalary(employees);
  }

}
