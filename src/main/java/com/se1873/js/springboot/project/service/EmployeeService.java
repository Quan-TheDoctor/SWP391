package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
  private final EmploymentHistoryRepository employmentHistoryRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final ContractRepository contractRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public Page<EmployeeDTO> getAll(Pageable pageable) {
    return employeeRepository.findAll(pageable).map(this::convertEmployeeToDTO);
  }

  public Page<EmployeeDTO> getEmployeesByDepartmentId(Integer departmentId, Pageable pageable) {
    List<EmployeeDTO> employeeDTOS = new ArrayList<>();
    var employees = getAll(pageable);
    for(var emp : employees.getContent()) {
      if(emp.getDepartmentId().equals(departmentId)) employeeDTOS.add(emp);
    }

    return new PageImpl<>(employeeDTOS, pageable, employeeDTOS.size());
  }

  public EmployeeDTO getEmployeeByEmployeeId(Integer employeeId) {
    return convertEmployeeToDTO(employeeRepository.getEmployeeByEmployeeId(employeeId));
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
    Page<Employee> employees;
    if ("all".equals(value)) {
      employees = employeeRepository.findAll(pageable);
    } else if ("department".equals(field)) {
      employees = employeeRepository.findEmployeesByDepartmentName(value, pageable);
    } else if ("position".equals(field)) {
      employees = employeeRepository.findEmployeesByPositionName(value, pageable);
    } else {
      throw new IllegalArgumentException("Invalid field: " + field);
    }

    return employees.map(this::convertEmployeeToDTO);
  }

  public Page<EmployeeDTO> search(Pageable pageable, String query) {
    String[] search = query.trim().split(" ", 2);
    String firstName;
    String lastName;
    if (search.length > 1) {
      firstName = search[0];
      lastName = search[1];
    } else {
      firstName = query;
      lastName = query;
    }
    Page<Employee> employees = employeeRepository.searchEmployee(firstName, lastName, pageable);
    return employees.map(this::convertEmployeeToDTO);
  }

  private EmployeeDTO convertEmployeeToDTO(Employee employee) {
    EmploymentHistory employmentHistory =
      employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employee.getEmployeeId(), true);

    EmployeeDTO.EmployeeDTOBuilder dtoBuilder = EmployeeDTO.builder()
      .employeeId(employee.getEmployeeId())
      .employeeCode(employee.getEmployeeCode())
      .employeeFirstName(employee.getFirstName())
      .employeeLastName(employee.getLastName())
      .employeeBirthDate(employee.getBirthDate())
      .employeeGender(employee.getGender())
      .employeeIdNumber(employee.getIdNumber())
      .employeePermanentAddress(employee.getPermanentAddress())
      .employeeTemporaryAddress(employee.getTemporaryAddress())
      .employeePersonalEmail(employee.getPersonalEmail())
      .employeeCompanyEmail(employee.getCompanyEmail())
      .employeePhone(employee.getPhoneNumber())
      .employeeMaritalStatus(employee.getMaritalStatus())
      .employeeBankAccount(employee.getBankAccount())
      .employeeBankName(employee.getBankName())
      .employeeTaxCode(employee.getTaxCode());

    if (employmentHistory != null) {
      Department department = departmentRepository.findDepartmentByDepartmentId(
        employmentHistory.getDepartment().getDepartmentId());
      Position position = positionRepository.findPositionByPositionId(
        employmentHistory.getPosition().getPositionId());

      dtoBuilder
        .departmentId(department.getDepartmentId())
        .departmentName(department.getDepartmentName())
        .positionId(position.getPositionId())
        .positionName(position.getPositionName())
        .employmentHistoryId(employmentHistory.getHistoryId())
        .employmentHistoryStartDate(employmentHistory.getStartDate())
        .employmentHistoryEndDate(employmentHistory.getEndDate())
        .employmentHistoryIsCurrent(employmentHistory.getIsCurrent());
    }

    Contract contract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(
      employee.getEmployeeId(), true);

    if (contract != null) {
      dtoBuilder
        .contractId(contract.getContractId())
        .contractType(contract.getContractType())
        .contractCode(contract.getContractCode())
        .contractStartDate(contract.getStartDate())
        .contractEndDate(contract.getEndDate())
        .contractBaseSalary(contract.getBaseSalary())
        .contractSignDate(contract.getSignDate());
    }

    return dtoBuilder.build();
  }

  public Resource exportToExcel(String departmentFilter, String positionFilter) {
    Pageable pageable = PageRequest.of(0, 1000);
    List<Employee> employees;

    if (!"all".equals(departmentFilter) && !"all".equals(positionFilter)) {
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

      Row headerRow = sheet.createRow(0);
      String[] columns = {"ID", "Name", "Department", "Position", "Salary"};
      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
      }

      int rowIdx = 1;
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
        row.createCell(4).setCellValue(contract.getBaseSalary());
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

    return employees.map(this::convertEmployeeToDTO);
  }

  /**
   * Save new / updated employee to the database based on data inside employeeDTO taken from html view
   * @param employeeDTO: variable that contains data needs
   */
  public void saveEmployee(EmployeeDTO employeeDTO) {
    Department department = departmentRepository.findDepartmentByDepartmentId(employeeDTO.getDepartmentId());
    Position position = positionRepository.findPositionByPositionId(employeeDTO.getPositionId());

    if (employeeDTO.getEmployeeId() == null) {
      createNewEmployee(employeeDTO, department, position);
    } else {
      updateExistingEmployee(employeeDTO, department, position);
    }
  }

  /**
   * Create new Employee in case that the user's creating new employee
   * Create new Employee, User, EmploymentHistory, Contract
   * @param employeeDTO: variable that contains data needs, passed from saveEmployee()
   * @param department: variable that contains department data
   * @param position: variable that contains position data
   */
  private void createNewEmployee(EmployeeDTO employeeDTO, Department department, Position position) {
    Employee employee = new Employee();
    editEmployee(employee, employeeDTO);
    employee = employeeRepository.save(employee);

    createUserAccount(employee, employeeDTO.getEmployeeCompanyEmail());

    createEmploymentHistory(employee, department, position, employeeDTO);

    createContract(employee, employeeDTO);
  }

  /**
   * Update existing Employee with given data
   * Update EmploymentHistory, Contract in case of any changes
   * @param employeeDTO: variable that contains data needs, passed from saveEmployee()
   * @param department: variable that contains department data
   * @param position: variable that contains position data
   */
  private void updateExistingEmployee(EmployeeDTO employeeDTO, Department department, Position position) {
    Employee employee = employeeRepository.getEmployeeByEmployeeId(employeeDTO.getEmployeeId());
    editEmployee(employee, employeeDTO);
    employeeRepository.save(employee);

    updateEmploymentHistoryIfNeeded(employee, department, position, employeeDTO);

    updateContractIfNeeded(employee, employeeDTO);
  }

  /**
   * Create a User for given Employee data
   * @param employee: variable that contains employee data
   * @param email: variable that contains company email, passed from EmployeeDTO in createNewEmployee()
   */
  private void createUserAccount(Employee employee, String email) {
    User user = User.builder()
      .username(email)
      .passwordHash(passwordEncoder.encode("1"))
      .role("USER")
      .employee(employee)
      .build();

    userRepository.save(user);
  }

  /**
   * Create an EmploymentHistory for given Employee data
   * @param employee:  variable that contains employee data
   * @param department: variable that contains department data
   * @param position: variable that contains position data
   * @param employeeDTO: variable that contains data needs, passed from saveEmployee()
   */
  private void createEmploymentHistory(Employee employee, Department department, Position position, EmployeeDTO employeeDTO) {
    EmploymentHistory employmentHistory = EmploymentHistory.builder()
      .employee(employee)
      .department(department)
      .position(position)
      .startDate(employeeDTO.getEmploymentHistoryStartDate())
      .endDate(employeeDTO.getEmploymentHistoryEndDate())
      .isCurrent(true)
      .build();

    employmentHistoryRepository.save(employmentHistory);
  }

  /**
   * Create a Contract for given Employee data
   * @param employee:  variable that contains employee data
   * @param employeeDTO: variable that contains data needs, passed from saveEmployee()
   */
  private void createContract(Employee employee, EmployeeDTO employeeDTO) {
    Contract contract = Contract.builder()
      .employee(employee)
      .contractCode(employeeDTO.getContractCode())
      .contractType(employeeDTO.getContractType())
      .startDate(employeeDTO.getContractStartDate())
      .endDate(employeeDTO.getContractEndDate())
      .baseSalary(employeeDTO.getContractBaseSalary())
      .signDate(employeeDTO.getContractSignDate())
      .isPresent(true)
      .build();

    contractRepository.save(contract);
  }

  /**
   * Update existing EmploymentHistory in case needed, close if needed
   * @param employee: variable that contains employee data
   * @param department: variable that contains department data
   * @param position: variable that contains position data
   * @param employeeDTO: variable that contains data needs, passed from saveEmployee()
   */
  private void updateEmploymentHistoryIfNeeded(Employee employee, Department department, Position position, EmployeeDTO employeeDTO) {
    EmploymentHistory currentHistory =
      employmentHistoryRepository.findEmploymentHistoryByEmployee_EmployeeIdAndIsCurrent(employeeDTO.getEmployeeId(), true);

    if (currentHistory == null) {
      createEmploymentHistory(employee, department, position, employeeDTO);
      return;
    }

    boolean positionChanged = !currentHistory.getPosition().getPositionId().equals(employeeDTO.getPositionId());
    boolean departmentChanged = !currentHistory.getDepartment().getDepartmentId().equals(department.getDepartmentId());

    if (positionChanged || departmentChanged) {
      currentHistory.setIsCurrent(false);
      currentHistory.setEndDate(LocalDate.now());
      employmentHistoryRepository.save(currentHistory);

      EmploymentHistory newEmploymentHistory = EmploymentHistory.builder()
        .employee(employee)
        .department(department)
        .position(position)
        .startDate(LocalDate.now())
        .isCurrent(true)
        .build();

      employmentHistoryRepository.save(newEmploymentHistory);
    }
  }

  /**
   * Create new or update existing Contract in case needed, close if needed
   * @param employee: variable that contains employee data
   * @param employeeDTO: variable that contains data needs, passed from saveEmployee()
   */
  private void updateContractIfNeeded(Employee employee, EmployeeDTO employeeDTO) {
    Contract currentContract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employeeDTO.getEmployeeId(), true);

    if (currentContract == null) {
      createContract(employee, employeeDTO);
      return;
    }

    if (currentContract.getContractCode().equals(employeeDTO.getContractCode())) {
      updateExistingContract(currentContract, employee, employeeDTO);
    } else {
      currentContract.setPresent(false);
      contractRepository.save(currentContract);

      createContract(employee, employeeDTO);
    }
  }

  private void updateExistingContract(Contract contract, Employee employee, EmployeeDTO employeeDTO) {
    contract.setEmployee(employee);
    contract.setContractCode(employeeDTO.getContractCode());
    contract.setContractType(employeeDTO.getContractType());
    contract.setStartDate(employeeDTO.getContractStartDate());
    contract.setEndDate(employeeDTO.getContractEndDate());
    contract.setBaseSalary(employeeDTO.getContractBaseSalary());
    contract.setSignDate(employeeDTO.getContractSignDate());
    contract.setPresent(true);

    contractRepository.save(contract);
  }
}
