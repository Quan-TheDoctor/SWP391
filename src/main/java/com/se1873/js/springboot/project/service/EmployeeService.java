package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.DependentDTO;
import com.se1873.js.springboot.project.dto.EmployeeCountDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.mapper.DependentDTOMapper;
import com.se1873.js.springboot.project.mapper.EmployeeDTOMapper;
import com.se1873.js.springboot.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
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
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
  private final DependentDTOMapper dependentDTOMapper;
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final ContractRepository contractRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final EmployeeDTOMapper employeeDTOMapper;

  public Page<EmployeeDTO> getAll(Pageable pageable) {
    Page<Employee> employeePage = employeeRepository.findAll(pageable);

    List<EmployeeDTO> filteredList = employeePage.getContent().stream()
      .map(employeeDTOMapper::toDTO)
      .filter(e -> !e.getIsDeleted())
      .collect(Collectors.toList());

    return new PageImpl<>(
      filteredList,
      pageable,
      employeePage.getTotalElements()
    );
  }


  public Page<EmployeeDTO> getEmployeesByDepartmentId(Integer departmentId, Pageable pageable) {
    return filterEmployeesByCondition(
      getAll(pageable),
      emp -> emp.getDepartmentId().equals(departmentId),
      pageable
    );
  }

  private Page<EmployeeDTO> filterEmployeesByCondition(Page<EmployeeDTO> source,
                                                       java.util.function.Predicate<EmployeeDTO> condition,
                                                       Pageable pageable) {
    List<EmployeeDTO> filtered = source.getContent()
      .stream()
      .filter(condition)
      .collect(Collectors.toList());
    return new PageImpl<>(filtered, pageable, filtered.size());
  }

  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return employeeDTOMapper.toDTO(employeeRepository.getEmployeeByEmployeeId(employeeId));
  }

  public Page<EmployeeDTO> sort(Page<EmployeeDTO> source, String direction, String field) {
    if (source == null)
      source = getAll(PageRequest.of(0, 5));
    List<EmployeeDTO> sorted = source.getContent().stream()
      .sorted(getComparator(field, direction))
      .collect(Collectors.toList());

    return new PageImpl<>(sorted, source.getPageable(), sorted.size());
  }

  private java.util.Comparator<EmployeeDTO> getComparator(String field, String direction) {
    return switch (field) {
      case "firstName" ->
        Comparator.comparing(EmployeeDTO::getEmployeeFirstName, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "departmentName" ->
        Comparator.comparing(EmployeeDTO::getDepartmentName, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "positionName" ->
        Comparator.comparing(EmployeeDTO::getPositionName, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "startDate" ->
        Comparator.comparing(EmployeeDTO::getEmploymentHistoryStartDate, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      case "salary" ->
        Comparator.comparing(EmployeeDTO::getContractBaseSalary, direction.equals("asc") ? Comparator.reverseOrder() : Comparator.naturalOrder());
      default -> Comparator.comparing(EmployeeDTO::getEmployeeFirstName, Comparator.naturalOrder());
    };
  }

  private void editEmployee(Employee employee, EmployeeDTO employeeDTO) {
    employee.setFirstName(employeeDTO.getEmployeeFirstName());
    employee.setLastName(employeeDTO.getEmployeeLastName());
    employee.setBirthDate(employeeDTO.getEmployeeBirthDate());
    employee.setGender(employeeDTO.getEmployeeGender());
    employee.setIdNumber(employeeDTO.getEmployeeIdNumber());
    employee.setPermanentAddress(employeeDTO.getEmployeePermanentAddress());
    employee.setTemporaryAddress(employeeDTO.getEmployeeTemporaryAddress());
    employee.setPersonalEmail(employeeDTO.getEmployeePersonalEmail());
    employee.setPhoneNumber(employeeDTO.getEmployeePhone());
    employee.setMaritalStatus(employeeDTO.getEmployeeMaritalStatus());
    employee.setBankAccount(employeeDTO.getEmployeeBankAccount());
    employee.setBankName(employeeDTO.getEmployeeBankName());
    employee.setTaxCode(employeeDTO.getEmployeeTaxCode());
    employee.setEmployeeCode(employeeDTO.getEmployeeCode());
    employee.setCompanyEmail(employeeDTO.getEmployeeCompanyEmail());

  }

  public Page<EmployeeDTO> filterByField(String field, String value, Pageable pageable) {
    return switch (field.toLowerCase()) {
      case "department" ->
        employeeRepository.findEmployeesByDepartmentName(value, pageable).map(employeeDTOMapper::toDTO);
      case "position" -> employeeRepository.findEmployeesByPositionName(value, pageable).map(employeeDTOMapper::toDTO);
      case "all" -> getAll(pageable);
      default -> throw new IllegalArgumentException("Invalid field: " + field);
    };
  }

  public Page<EmployeeDTO> search(Pageable pageable, String query) {
    String[] searchTerms = query.trim().split(" ", 2);
    String firstName = searchTerms[0];
    String lastName = searchTerms.length > 1 ? searchTerms[1] : searchTerms[0];

    return employeeRepository.searchEmployee(firstName, lastName, pageable)
      .map(employeeDTOMapper::toDTO);
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

  /**
   * Save new / updated employee to the database based on data inside employeeDTO taken from html view
   *
   * @param employeeDTO: variable that contains data needs
   */
  public void saveEmployee(EmployeeDTO employeeDTO) {
    Department department = departmentRepository.findDepartmentByDepartmentId(employeeDTO.getDepartmentId());
    Position position = positionRepository.findPositionByPositionId(employeeDTO.getPositionId());

    if (employeeDTO.getEmployeeId() == null) {
      createNewEmployeeWithRelatedRecords(employeeDTO, department, position);
    } else {
      updateExistingEmployeeWithRelatedRecords(employeeDTO, department, position);
    }
  }

  // region Core Business Logic
  private void createNewEmployeeWithRelatedRecords(EmployeeDTO dto, Department department, Position position) {
    Employee employee = createAndSaveBaseEmployee(dto);
    createUserAccount(employee, dto.getEmployeeCompanyEmail());
    createInitialEmploymentHistory(employee, department, position, dto);
    createInitialContract(employee, dto);

    if (dto.getDependents() != null) {
      handleDependents(employee, dto.getDependents());
    }

    employeeRepository.save(employee);
  }

  private void updateExistingEmployeeWithRelatedRecords(EmployeeDTO dto, Department department, Position position) {
    Employee employee = employeeRepository.getEmployeeByEmployeeId(dto.getEmployeeId());
    updateEmployeeDetails(employee, dto);
    handleEmploymentHistoryChanges(employee, department, position, dto);
    handleContractChanges(employee, dto);

    if (dto.getDependents() != null) {
      handleDependents(employee, dto.getDependents());
    }

    employeeRepository.save(employee);
  }

  // endregion
  // region Employee Management Helpers
  private Employee createAndSaveBaseEmployee(EmployeeDTO dto) {
    Employee employee = new Employee();
    updateEmployeeDetails(employee, dto);
    return employeeRepository.save(employee);
  }

  private void updateEmployeeDetails(Employee employee, EmployeeDTO dto) {
    employee.setFirstName(dto.getEmployeeFirstName());
    employee.setLastName(dto.getEmployeeLastName());
    employee.setBirthDate(dto.getEmployeeBirthDate());
    employee.setGender(dto.getEmployeeGender());
    employee.setIdNumber(dto.getEmployeeIdNumber());
    employee.setPermanentAddress(dto.getEmployeePermanentAddress());
    employee.setTemporaryAddress(dto.getEmployeeTemporaryAddress());
    employee.setPersonalEmail(dto.getEmployeePersonalEmail());
    employee.setPhoneNumber(dto.getEmployeePhone());
    employee.setMaritalStatus(dto.getEmployeeMaritalStatus());
    employee.setBankAccount(dto.getEmployeeBankAccount());
    employee.setBankName(dto.getEmployeeBankName());
    employee.setTaxCode(dto.getEmployeeTaxCode());
    employee.setEmployeeCode(dto.getEmployeeCode());
    employee.setCompanyEmail(dto.getEmployeeCompanyEmail());
    employee.setPicture(dto.getPicture());
    employee.setIsDeleted(false);
  }

  // endregion
  // region Employment History Management
  private void createInitialEmploymentHistory(Employee employee, Department department, Position position, EmployeeDTO dto) {
    EmploymentHistory history = EmploymentHistory.builder()
      .employee(employee)
      .department(department)
      .position(position)
      .startDate(dto.getEmploymentHistoryStartDate())
      .endDate(dto.getEmploymentHistoryEndDate())
      .isCurrent(true)
      .build();
    employmentHistoryRepository.save(history);
  }

  private void handleEmploymentHistoryChanges(Employee employee, Department department, Position position, EmployeeDTO dto) {
    EmploymentHistory currentHistory = employmentHistoryRepository
      .findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employee.getEmployeeId(), true);

    if (currentHistory == null) {
      createInitialEmploymentHistory(employee, department, position, dto);
      return;
    }

    if (shouldUpdateEmploymentHistory(currentHistory, department, position)) {
      closeCurrentEmploymentHistory(currentHistory);
      createNewEmploymentHistory(employee, department, position);
    }
  }

  private boolean shouldUpdateEmploymentHistory(EmploymentHistory current, Department newDept, Position newPos) {
    return !current.getDepartment().getDepartmentId().equals(newDept.getDepartmentId()) ||
      !current.getPosition().getPositionId().equals(newPos.getPositionId());
  }

  private void closeCurrentEmploymentHistory(EmploymentHistory history) {
    history.setIsCurrent(false);
    history.setEndDate(LocalDate.now());
    employmentHistoryRepository.save(history);
  }

  private void createNewEmploymentHistory(Employee employee, Department department, Position position) {
    EmploymentHistory newHistory = EmploymentHistory.builder()
      .employee(employee)
      .department(department)
      .position(position)
      .startDate(LocalDate.now())
      .isCurrent(true)
      .build();
    employmentHistoryRepository.save(newHistory);
  }

  // endregion
  // region Contract Management
  private void createInitialContract(Employee employee, EmployeeDTO dto) {
    Contract contract = Contract.builder()
      .employee(employee)
      .contractCode(dto.getContractCode())
      .contractType(dto.getContractType())
      .startDate(dto.getContractStartDate())
      .endDate(dto.getContractEndDate())
      .baseSalary(dto.getContractBaseSalary())
      .signDate(dto.getContractSignDate())
      .isPresent(true)
      .build();
    contractRepository.save(contract);
  }

  private void handleContractChanges(Employee employee, EmployeeDTO dto) {
    Contract currentContract = contractRepository
      .findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);

    if (currentContract == null) {
      createInitialContract(employee, dto);
      return;
    }

    if (isNewContractNeeded(currentContract, dto)) {
      closeCurrentContract(currentContract);
      createInitialContract(employee, dto);
    } else {
      updateExistingContract(currentContract, dto);
    }
  }

  private boolean isNewContractNeeded(Contract current, EmployeeDTO dto) {
    return !current.getContractCode().equals(dto.getContractCode());
  }

  private void closeCurrentContract(Contract contract) {
    contract.setPresent(false);
    contractRepository.save(contract);
  }

  private void updateExistingContract(Contract contract, EmployeeDTO dto) {
    contract.setContractCode(dto.getContractCode());
    contract.setContractType(dto.getContractType());
    contract.setStartDate(dto.getContractStartDate());
    contract.setEndDate(dto.getContractEndDate());
    contract.setBaseSalary(dto.getContractBaseSalary());
    contract.setSignDate(dto.getContractSignDate());
    contractRepository.save(contract);
  }

  // endregion
  // region User Account Management
  private void createUserAccount(Employee employee, String email) {
    User user = User.builder()
      .username(email)
      .passwordHash(passwordEncoder.encode("1"))
      .role("USER")
      .employee(employee)
      .build();
    userRepository.save(user);
  }

  // endregion
// region Dependent Management
  private void handleDependents(Employee employee, List<DependentDTO> dependentDTOs) {
    if (employee.getDependents() == null) {
      employee.setDependents(new ArrayList<>());
    }

    if (dependentDTOs == null || dependentDTOs.isEmpty()) {
      return;
    }

    List<DependentDTO> validDependents = dependentDTOs.stream()
      .filter(dep -> dep != null && dep.getFullName() != null && !dep.getFullName().isEmpty())
      .collect(Collectors.toList());

    if (validDependents.isEmpty()) {
      return;
    }

    if (!employee.getDependents().isEmpty()) {
      Map<Integer, Dependent> existingDependentsMap = employee.getDependents().stream()
        .collect(Collectors.toMap(Dependent::getDependentId, Function.identity(), (a, b) -> a));

      for (DependentDTO dependentDTO : validDependents) {
        if (dependentDTO.getDependentId() != null && existingDependentsMap.containsKey(dependentDTO.getDependentId())) {
          Dependent existingDependent = existingDependentsMap.get(dependentDTO.getDependentId());
          updateDependentDetails(existingDependent, dependentDTOMapper.toEntity(dependentDTO));
          existingDependentsMap.remove(dependentDTO.getDependentId());
        } else {
          createNewDependent(employee, dependentDTOMapper.toEntity(dependentDTO));
        }
      }
    } else {
      for (DependentDTO dependentDTO : validDependents) {
        createNewDependent(employee, dependentDTOMapper.toEntity(dependentDTO));
      }
    }

    log.info("Added {} dependents to employee", employee.getDependents().size());
  }

  private void createNewDependent(Employee employee, Dependent dependentDTO) {
    Dependent dependent = new Dependent();
    updateDependentDetails(dependent, dependentDTO);
    dependent.setEmployee(employee);

    employee.getDependents().add(dependent);
    log.info("Added new dependent: {}", dependent.getFullName());
  }

  private void updateDependentDetails(Dependent dependent, Dependent dependentDTO) {
    dependent.setFullName(dependentDTO.getFullName());
    dependent.setRelationship(dependentDTO.getRelationship());
    dependent.setBirthDate(dependentDTO.getBirthDate());
    dependent.setIdNumber(dependentDTO.getIdNumber());
    dependent.setIsTaxDependent(dependentDTO.getIsTaxDependent());
  }
// endregion


  public int countEmployees() {
    return employeeRepository.getEmployeeCount();
  }

  public EmployeeCountDTO getEmployeeDTOIsCurrent(){
    EmployeeCountDTO employeeCountDTO = EmployeeCountDTO.builder()
            .totalAvailableEmployees(0).totalUnavailableEmployees(0).build();
    List<EmployeeDTO> employeeDTO = employeeRepository.findAll()
            .stream().map(employeeDTOMapper::toDTO)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    for (EmployeeDTO employeeDTO1 : employeeDTO) {
      System.out.println(employeeDTO1);
      if(Boolean.TRUE.equals(employeeDTO1.getContractIsPresent()) &&
              Boolean.TRUE.equals(employeeDTO1.getEmploymentHistoryIsCurrent())){
        employeeCountDTO.setTotalAvailableEmployees(employeeCountDTO.getTotalAvailableEmployees() + 1);
      } else employeeCountDTO.setTotalUnavailableEmployees(employeeCountDTO.getTotalUnavailableEmployees() + 1) ;
    }

    return employeeCountDTO;
  }


}
