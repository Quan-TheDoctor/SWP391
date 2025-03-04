package com.se1873.js.springboot.project.mapper;

import java.util.List;

public interface EntityMapper <D, E> {
  public D toDTO(E user);
  public E toEntity(D employeeDTO);
  public List<D> toDTO(List<E> userList);
  public List<E> toEntity(List<D> employeeDTOList);
}
