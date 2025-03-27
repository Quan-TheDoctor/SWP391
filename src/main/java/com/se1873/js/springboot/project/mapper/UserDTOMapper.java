package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.RoleDTO;
import com.se1873.js.springboot.project.dto.UserDTO;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
  componentModel = "spring",
  uses = {
    EmployeeDTOMapper.class,
    AuditLogDTOMapper.class
  }
)
public abstract class UserDTOMapper {
  @Mapping(target = "employeeId", ignore = true)
  @Mapping(target = "employeeCode", ignore = true)
  @Mapping(target = "employeeFirstName", ignore = true)
  @Mapping(target = "employeeLastName", ignore = true)
  @Mapping(target = "employeeGender", ignore = true)
  @Mapping(target = "employeeBirthDate", ignore = true)
  @Mapping(target = "employeeBankAccount", ignore = true)
  @Mapping(target = "employeeBankName", ignore = true)
  @Mapping(target = "employeeTemporaryAddress", ignore = true)
  @Mapping(target = "employeePermanentAddress", ignore = true)
  @Mapping(target = "employeeIdNumber", ignore = true)
  @Mapping(target = "employeeTaxCode", ignore = true)
  @Mapping(target = "employeePersonalEmail", ignore = true)
  @Mapping(target = "employeeCompanyEmail", ignore = true)
  @Mapping(target = "employeePhone", ignore = true)
  @Mapping(target = "employeeMaritalStatus", ignore = true)
  public abstract UserDTO toDTO(User user);

  @AfterMapping
  protected void mapNestedFields(
    User user,
    @MappingTarget UserDTO userDTO
  ) {
    userDTO.setUserId(user.getUserId());
    userDTO.setUsername(user.getUsername());
    userDTO.setRole(user.getRole());
    userDTO.setStatus(user.getStatus());
    userDTO.setLastLogin(user.getLastLogin());
    userDTO.setCreatedAt(user.getCreatedAt());
    userDTO.setUpdatedAt(user.getUpdatedAt());

    if (user.getEmployee() != null) {
      userDTO.setEmployeeId(user.getEmployee().getEmployeeId());
      userDTO.setEmployeeCode(user.getEmployee().getEmployeeCode());
      userDTO.setEmployeeFirstName(user.getEmployee().getFirstName());
      userDTO.setEmployeeLastName(user.getEmployee().getLastName());
      userDTO.setEmployeeGender(user.getEmployee().getGender());
      userDTO.setEmployeeBirthDate(user.getEmployee().getBirthDate());
      userDTO.setEmployeeBankAccount(user.getEmployee().getBankAccount());
      userDTO.setEmployeeBankName(user.getEmployee().getBankName());
      userDTO.setEmployeeTemporaryAddress(user.getEmployee().getTemporaryAddress());
      userDTO.setEmployeePermanentAddress(user.getEmployee().getPermanentAddress());
      userDTO.setEmployeeIdNumber(user.getEmployee().getIdNumber());
      userDTO.setEmployeeTaxCode(user.getEmployee().getTaxCode());
      userDTO.setEmployeePersonalEmail(user.getEmployee().getPersonalEmail());
      userDTO.setEmployeeCompanyEmail(user.getEmployee().getCompanyEmail());
      userDTO.setEmployeePhone(user.getEmployee().getPhoneNumber());
      userDTO.setEmployeeMaritalStatus(user.getEmployee().getMaritalStatus());
    }
  }

  public abstract List<UserDTO> toDTOList(List<User> users);

  @Mapping(target = "employee", ignore = true)
  @Mapping(target = "auditLogs", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  public abstract User toEntity(UserDTO userDTO);

  @AfterMapping
  protected void afterToEntity(UserDTO userDTO, @MappingTarget User user) {

  }
}
