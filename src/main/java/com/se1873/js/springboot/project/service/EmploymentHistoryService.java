package com.se1873.js.springboot.project.service;

import com.se1873.js.springboot.project.repository.EmploymentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmploymentHistoryService {
    @Autowired
    private EmploymentHistoryRepository employmentHistoryRepository;

}
