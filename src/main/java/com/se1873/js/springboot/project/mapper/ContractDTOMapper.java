package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.ContractDTO;
import com.se1873.js.springboot.project.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractDTOMapper extends EntityMapper<ContractDTO, Contract> {
  @Override
  @Mapping(source = "contractId", target = "contractId")
  @Mapping(source = "createdAt", target = "createdAt")
  ContractDTO toDTO(Contract contract);

  @Override
  @Mapping(source = "contractId", target = "contractId")
  @Mapping(source = "createdAt", target = "createdAt")
  Contract toEntity(ContractDTO contractDTO);
}
