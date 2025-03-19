package com.se1873.js.springboot.project.repository;

import com.se1873.js.springboot.project.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRepository extends JpaRepository<Leave,Long> {
}
