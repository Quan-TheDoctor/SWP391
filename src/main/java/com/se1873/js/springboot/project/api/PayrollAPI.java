package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.PayrollChartDTO;
import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/payroll")
public class PayrollAPI {

    private final SalaryRecordService salaryRecordService;

    @RequestMapping("/totalpayrollByYear")
    public ResponseEntity<List<PayrollChartDTO>> getSalaryByYear(@RequestParam("year") int year) {
        List<PayrollChartDTO> payrollChartDTOList = new ArrayList<>();
        var statistic = salaryRecordService.getMonthlySalary(year);
        for(var s : statistic.entrySet()) {
            PayrollChartDTO payrollChartDTO = new PayrollChartDTO();
            payrollChartDTO.setMonth(s.getKey());
            payrollChartDTO.setTotal(s.getValue());
            payrollChartDTOList.add(payrollChartDTO);
        }

        return ResponseEntity.ok(payrollChartDTOList);
    }
}
