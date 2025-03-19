package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.DepartmentDTO;
import com.se1873.js.springboot.project.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentDTOMapper extends EntityMapper<DepartmentDTO, Department> {
  @Mapping(source = "departmentId", target = "departmentId")
  @Mapping(source = "departmentCode", target = "departmentCode")
  @Mapping(source = "departmentName", target = "departmentName")
  @Mapping(source = "description", target = "departmentDescription")
  @Mapping(source = "createdAt", target = "departmentCreatedAt")
  @Mapping(source = "managerId", target = "managerId")
  DepartmentDTO toDTO(Department department);
}