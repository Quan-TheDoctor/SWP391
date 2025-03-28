package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.dto.LeaveDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.LeaveRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.LeavePolicyService;
import com.se1873.js.springboot.project.service.LeaveService;
import com.se1873.js.springboot.project.service.RequestService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.position.PositionService;
import com.se1873.js.springboot.project.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/leave")
@SessionAttributes({"leaves"})
public class LeaveController {
    private final LeaveService leaveService;
    private final DepartmentService departmentService;
    private final EmployeeRepository employeeRepository;
    private final PositionService positionService;
    private final LeavePolicyService leavePolicyService;
    private final EmployeeService employeeService;
    private final UserRepository userRepository;
    private final RequestService requestService;
    private final UserService userService;

    @ModelAttribute("leaves")
    public Page<Leave> initDataPage() {
        return Page.empty();
    }

    @RequestMapping
    @PreAuthorize("hasPermission('ATTENDANCE', 'VIEW')")
    public String leave(Model model,
                        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                        @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        var leaves = leaveService.getAllLeave(PageRequest.of(page, size));
        var leavePolicies = leavePolicyService.getAllLeavePolicies();

        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("leaves", leaves);
        model.addAttribute("leavePolicies", leavePolicies);
        model.addAttribute("contentFragment", "fragments/leave-fragments");
        return "index";
    }

    @RequestMapping("/leave-policies")
    @PreAuthorize("hasPermission('SYSTEM', 'VIEW')")
    public String leavepolicies(Model model) {
        List<LeavePolicy> policies = leavePolicyService.getAllLeavePolicies();
        model.addAttribute("policies", policies);
        return "fragments/leave-fragments";
    }

    @RequestMapping("/leavefilter")
    @PreAuthorize("hasPermission('ATTENDANCE', 'VIEW')")
    public String leavefilter(@RequestParam("value") String leaveType, Model model, Pageable pageable,
                              @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                              @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        if (leaveType.equals("all")) {
            var leaves = leaveService.getAllLeave(PageRequest.of(page, size));
            model.addAttribute("leaves", leaves);
        } else {
            var leaves = leaveService.filterLeaveByLeaveType(leaveType, pageable);
            model.addAttribute("leaves", leaves);
        }

        List<LeavePolicy> leavePolicies = leavePolicyService.getAllLeavePolicies();
        model.addAttribute("leavePolicies", leavePolicies);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "leave";
    }

    @RequestMapping("/sort")
    @PreAuthorize("hasPermission('ATTENDANCE', 'VIEW')")
    public String sort(@RequestParam("field") String field,
                       @RequestParam("direction") String direction,
                       Model model, Pageable pageable,
                       @ModelAttribute("leaves") Page<Leave> leaveList,
                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                       @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        Page<Leave> leaves = leaveService.sortLeave(leaveList, PageRequest.of(page, size), field, direction);

        List<LeavePolicy> leavePolicies = leavePolicyService.getAllLeavePolicies();
        model.addAttribute("direction", direction);
        model.addAttribute("leavePolicies", leavePolicies);
        model.addAttribute("leaves", leaves);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "leave";
    }

    @GetMapping("/create-leave")
    @PreAuthorize("hasPermission('REQUEST', 'ADD')")
    public String createLeaveRequest(
            @RequestParam(name = "department", required = false) Integer departmentId,
            @RequestParam(name = "position", required = false) String positionName,
            @RequestParam(name = "employee", required = false) Integer employeeId,
            @RequestParam(name = "leaveType", required = false) String leaveType,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            @RequestParam(name = "reason", required = false) String reason,
            Model model) {
        List<Department> departmentList = departmentService.getAllDepartments();
        long daysBetween = 0;
        if (startDate != null && endDate != null && startDate.isBefore(endDate)) {
             daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        }
        List<Position> positionList = new ArrayList<>();
        if (departmentId != null) {
            positionList = positionService.getPositionsByDepartmentId(departmentId);
        }

        List<Employee> employeeList = new ArrayList<>();
        if (positionName != null) {
            employeeList = employeeService.getEmployeesByPositionName(positionName);
        }
        EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(employeeId);

        List<LeavePolicy> leavePolicies = leavePolicyService.getAllLeavePolicies();
        LeaveDTO leaveRequestDTO = new LeaveDTO();
        leaveRequestDTO.setDepartmentId(departmentId);
        leaveRequestDTO.setPosition(positionName);
        leaveRequestDTO.setEmployee(employee);
        leaveRequestDTO.setLeaveType(leaveType);
        leaveRequestDTO.setStartDate(startDate);
        leaveRequestDTO.setEndDate(endDate);
        leaveRequestDTO.setTotalDays((int) daysBetween);
        leaveRequestDTO.setReason(reason);

        if (employee != null && leaveType != null) {
            Map<String, Integer> leaveBalance = leavePolicyService.getLeaveBalance(employee.getEmployeeId(), leaveType);
            model.addAttribute("leaveBalance", leaveBalance);
        }

        model.addAttribute("leavePolicies", leavePolicies);
        model.addAttribute("employeeList", employeeList);
        model.addAttribute("positionList", positionList);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("daysBetween", daysBetween);
        model.addAttribute("leaveRequest", leaveRequestDTO);
        return "fragments/create-leave";
    }

    @PostMapping("/submit-leave")
    @PreAuthorize("hasPermission('REQUEST', 'ADD')")
    public String submitLeave( @RequestParam("employeeId") Integer employeeId,
                               @RequestParam("department") Integer departmentId,
                              @RequestParam("leaveType") String leaveType,
                              @RequestParam("reason") String reason,
                              @RequestParam("startDate") String startDate,
                              @RequestParam("endDate") String endDate,
                              @RequestParam("totalDays") Integer totalDays,
                              RedirectAttributes redirectAttributes) {
        try {
            RequestDTO requestDTO = new RequestDTO();

            LeaveDTO leave = new LeaveDTO();
            leave.setLeaveType(leaveType);
            leave.setReason(reason);
            leave.setStartDate(LocalDate.parse(startDate));
            leave.setEndDate(LocalDate.parse(endDate));
            leave.setTotalDays(totalDays);

            LeavePolicy leavePolicy = leavePolicyService.getAllLeavePolicies().stream()
                .filter(policy -> policy.getLeavePolicyName().equals(leaveType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách nghỉ phép cho loại: " + leaveType));
            leave.setLeavePolicyId(leavePolicy.getLeavePolicyId());
            
            requestDTO.setPayrollIds(Collections.singletonList(employeeId));
            System.out.println("check total day: " + totalDays);
            requestDTO.setLeaveDTO(leave);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();
            User user = userRepository.findUserByUsername(name)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            Employee employee = employeeRepository.getEmployeeByEmployeeId(employeeId);
            requestService.save(requestDTO, user, employee);

            Optional<User> getUser = userService.getManagerByDepartmentId(departmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn xin nghỉ phép đã được tạo thành công!");
            return "redirect:/leave";
        } catch (Exception e) {
            log.error("Error submitting leave request", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tạo đơn xin nghỉ phép: " + e.getMessage());
            return "redirect:/leave/create-leave";
        }
    }
}
