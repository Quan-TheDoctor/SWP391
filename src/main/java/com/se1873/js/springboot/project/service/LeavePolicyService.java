package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.entity.LeavePolicy;
import com.se1873.js.springboot.project.repository.LeavePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;

    public List<LeavePolicy> getAllLeavePolicies() {return leavePolicyRepository.findAll();}
}
