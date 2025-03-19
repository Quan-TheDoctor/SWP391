package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.entity.LeavePolicy;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.service.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/leave")
public class LeaveController {

    private final LeavePolicyService leavePolicyService;
    @RequestMapping
    public String leave() {
        return "leave";
    }

    @RequestMapping("/policies")
    public String leavepolicies(Model model) {
        List<LeavePolicy> policies = leavePolicyService.getAllLeavePolicies();
        model.addAttribute("policies", policies);
        model.addAttribute("contentFragment", "fragments/leave-policies-fragments");
        return "index";
    }
}
