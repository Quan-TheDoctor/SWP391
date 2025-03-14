package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.DependentDTO;
import com.se1873.js.springboot.project.entity.Dependent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DependentDTOMapper {

  DependentDTOMapper INSTANCE = Mappers.getMapper(DependentDTOMapper.class);

  @Mapping(target = "employeeId", source = "employee.employeeId")
  DependentDTO toDTO(Dependent dependent);

  @Mapping(target = "employee", ignore = true)
  Dependent toEntity(DependentDTO dependentDTO);
}
