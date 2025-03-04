package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.PositionDTO;
import com.se1873.js.springboot.project.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PositionDTOMapper extends EntityMapper<PositionDTO, Position> {
  @Mapping(source = "positionId", target = "positionId")
  @Mapping(source = "positionCode", target = "positionCode")
  @Mapping(source = "positionName", target = "positionName")
  @Mapping(source = "level", target = "positionLevel")
  @Mapping(source = "description", target = "positionDescription")
  @Mapping(source = "createdAt", target = "positionCreatedAt")
  PositionDTO toDTO(Position position);
}