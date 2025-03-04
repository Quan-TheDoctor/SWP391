package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.EmploymentHistoryDTO;
import com.se1873.js.springboot.project.entity.EmploymentHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmploymentHistoryDTOMapper extends EntityMapper<EmploymentHistoryDTO, EmploymentHistory> {
  @Mapping(source = "historyId", target = "employmentHistoryId")
  @Mapping(source = "startDate", target = "employmentHistoryStartDate")
  @Mapping(source = "endDate", target = "employmentHistoryEndDate")
  @Mapping(source = "isCurrent", target = "employmentHistoryIsCurrent")
  @Mapping(source = "transferReason", target = "transferReason")
  @Mapping(source = "decisionNumber", target = "decisionNumber")
  @Mapping(source = "createdAt", target = "employmentHistoryCreatedAt")
  EmploymentHistoryDTO toDTO(EmploymentHistory employmentHistory);
}