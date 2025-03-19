package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.dto.AverageSalaryDTO;
import com.se1873.js.springboot.project.dto.PayrollChartDTO;
import com.se1873.js.springboot.project.dto.PayrollDTO;
import com.se1873.js.springboot.project.dto.TopSalaryDTO;
import com.se1873.js.springboot.project.service.SalaryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @RequestMapping("/averageDepartmentSalary")
    public ResponseEntity<List<AverageSalaryDTO>> averageSalaryDTOS(@RequestParam("year") int year) {
        List<AverageSalaryDTO> averageSalaryDTOList = salaryRecordService.getAverageSalaryByYear(year);
        log.info(averageSalaryDTOList.toString());
        return ResponseEntity.ok(averageSalaryDTOList);
    }

    @RequestMapping("/test")
    public ResponseEntity<List<TopSalaryDTO>> test(@RequestParam("year") int year,
                                                   @RequestParam("month") int month) {
        List<TopSalaryDTO> topSalaryDTOS = salaryRecordService.getTop3HighestNetSalary(month,year);

        return ResponseEntity.ok(topSalaryDTOS);
    }
}
