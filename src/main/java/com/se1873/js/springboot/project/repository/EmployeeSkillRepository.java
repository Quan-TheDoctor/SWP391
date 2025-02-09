package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
  List<EmployeeSkill> findAllByEmployee(Employee employee);
}
