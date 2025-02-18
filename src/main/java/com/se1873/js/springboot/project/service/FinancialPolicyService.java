package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.FinancialPolicy;
import com.se1873.js.springboot.project.repository.FinancialPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialPolicyService {

  private final FinancialPolicyRepository financialPolicyRepository;

  public List<FinancialPolicy> getAll() {
    return financialPolicyRepository.findAll();
  }
}
