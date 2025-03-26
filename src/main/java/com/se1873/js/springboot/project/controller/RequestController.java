package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.RequestCreationRequestDTO;
import com.se1873.js.springboot.project.dto.RequestCreationResponseDTO;
import com.se1873.js.springboot.project.dto.RequestDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import com.se1873.js.springboot.project.entity.Employee;
import com.se1873.js.springboot.project.entity.SalaryRecord;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.repository.AttendanceRepository;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.SalaryRecordRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.*;
import com.se1873.js.springboot.project.entity.*;
import com.se1873.js.springboot.project.repository.*;
import com.se1873.js.springboot.project.service.ChatNotificationService;
import com.se1873.js.springboot.project.service.department.DepartmentService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import com.se1873.js.springboot.project.service.salary_record.SalaryRecordService;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestController {
    private static final Integer DEFAULT_PAGE_SIZE = 5;
    private static final String REQUEST_STATUS_PENDING = "pending";
    private static final String REQUEST_TYPE = "requestTypes";
    private static final String CONTENT_FRAGMENT = "contentFragment";
    private static final String REQUEST_FRAGMENTS = "fragments/request-fragments";
    private static final String INDEX = "index";

  private final RequestService requestService;
  private final RequestRepository requestRepository;
  private final DepartmentService departmentService;
  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final SalaryRecordRepository salaryRecordRepository;
  private final ContractRepository contractRepository;
  private final EmployeeService employeeService;
  private final ReponseRepo reponseRepo;
  private final AttendanceService attendanceService;
  private final AttendanceRepository attendanceRepository;
  private final LeavePolicyRepository leavePolicyRepository;
  private final LeaveRepository leaveRepository;
  private final UserService userService;
  private final LeavePolicyService leavePolicyService;
  private final NotificationRepository notificationRepository;

    private Integer totalRequests = 0;
    private Integer totalPendingRequests = 0;
    private Integer totalFinishedRequests = 0;
    private Set<String> requestTypes = new HashSet<>();

    private Pageable getPageable(Integer page, Integer size) {
        return PageRequest.of(page, size);
    }

    private String populateRequestModel(Model model, Page<RequestDTO> requests, String viewName) {
        model.addAttribute("requests", requests);
        addRequestStatistics(requests, model);
        model.addAttribute(REQUEST_TYPE, requestService.getAllRequestTypes());
        model.addAttribute(CONTENT_FRAGMENT, REQUEST_FRAGMENTS);
        return INDEX;
    }
    private void addRequestStatistics(Page<RequestDTO> requests, Model model) {
        long totalPending = requests.stream()
                .filter(r -> REQUEST_STATUS_PENDING.equalsIgnoreCase(r.getRequestStatus()))
                .count();

        model.addAttribute("totalRequests", requests.getTotalElements());
        model.addAttribute("totalPendingRequests", totalPending);
        model.addAttribute("totalFinishedRequests", requests.getTotalElements() - totalPending);
    }

    @RequestMapping
    public String request(Model model,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "5") int size) {
        return populateRequestModel(model, requestService.getRequests(getPageable(page, size)), "request");
    }

    @RequestMapping("/filter")
    public String filter(Model model,
                         @RequestParam("field") String field,
                         @RequestParam("value") String value,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "5") int size) {
        var requests = requestService.filter(field, value, getPageable(page, size));
        addRequestStatistics(requests, model);
        model.addAttribute("requests", requests);
        model.addAttribute(REQUEST_TYPE, requestService.getAllRequestTypes());
        model.addAttribute(CONTENT_FRAGMENT, REQUEST_FRAGMENTS);
        return INDEX;
    }

    @RequestMapping("/search")
    public String search(Model model,
                         @RequestParam("query") String query,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "5") int size) {
        Page<RequestDTO> requests = requestService.searchRequests(query, getPageable(page, size));

        model.addAttribute("requests", requests);
        model.addAttribute("query", query);
        addRequestStatistics(requests, model);
        model.addAttribute(CONTENT_FRAGMENT, REQUEST_FRAGMENTS);
        return INDEX;
    }

    @RequestMapping("/export/view")
    public String exportView(Model model,
                             @RequestParam(value = "status", required = false, defaultValue = "all") String status,
                             @RequestParam(value = "type", required = false, defaultValue = "all") String type,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "5") int size) {
        Page<RequestDTO> requests = requestService.exportFilteredRequests(status, type, getPageable(page, size));

        model.addAttribute("requests", requests.getContent());
        model.addAttribute("totalRequests", requests.getTotalElements());
        model.addAttribute(REQUEST_TYPE, requestService.getAllRequestTypes());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute(CONTENT_FRAGMENT, "fragments/request-export-fragments");
        return INDEX;
    }

    @RequestMapping("/export")
    public ResponseEntity<Resource> exportRequests(
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @RequestParam(value = "type", required = false, defaultValue = "all") String type) {
        log.info("Exporting request data to Excel. Status: {}, Type: {}", status, type);
        Resource file = requestService.exportToExcel(status, type);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=requests.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @RequestMapping("/save")
    public String save(@ModelAttribute("requestDTO") RequestDTO requestDTO,
                       BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/user/detail";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        int id = user.getEmployee().getEmployeeId();
        Employee employee = employeeRepository.getEmployeeByEmployeeId(id);

        requestService.save(requestDTO, user, employee);
        return "redirect:/user/detail?success=true";
    }

    @RequestMapping("/view")
    public String view(Model model, @RequestParam("requestId") Integer requestId) {
        RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
        List<SalaryRecord> salaryRecords = new ArrayList<>();
        for (var r : requestDTO.getPayrollIds()) {
            SalaryRecord sr = salaryRecordRepository.findSalaryRecordBySalaryIdAndIsDeleted(r, false);
            salaryRecords.add(sr);
        }
        requestDTO.setSalaryRecords(salaryRecords);

        model.addAttribute("requestDTO", requestDTO);
        model.addAttribute(CONTENT_FRAGMENT, "fragments/request-view-fragments");
        return INDEX;
    }

    @GetMapping("salary")
    public String viewRequest(@RequestParam("requestId") Integer requestId, Model model) {
        response responseDetails = reponseRepo.findByRequestId((requestId))
                .orElseThrow(() -> new RuntimeException("Request not found"));

        model.addAttribute("responseDetails", responseDetails);
        return "fragments/requestDetailsPopup";
    }

    @GetMapping("/salarystatus")
    public String updateRequestStatus(@RequestParam("field") String field,
                                      @RequestParam("requestId") Integer requestId) {
        Request request = requestRepository.findRequestByRequestId(requestId);
        response responseDetails = reponseRepo.findByRequestId((requestId))
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if ("Approved".equals(field)) {
            String employeeCode=responseDetails.getEmployeeCode();
            Employee employee=employeeRepository.findByEmployeeCode(employeeCode)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            Contract employeeContract= contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(),true);
            employeeContract.setBaseSalary(responseDetails.getNewBaseSalary());
            contractRepository.save(employeeContract);
            request.setStatus("Approved");
        } else if ("Denied".equals(field)) {
            String employeeCode=responseDetails.getEmployeeCode();
            Employee employee=employeeRepository.findByEmployeeCode(employeeCode)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            Contract employeeContract= contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(),true);
            employeeContract.setBaseSalary(responseDetails.getOldBaseSalary());
            contractRepository.save(employeeContract);
            request.setStatus("Denied");
        }

        requestRepository.save(request);
        return "redirect:/request";
    }



    @RequestMapping("/status")
    public String approveRequest(Model model,
                                 @RequestParam("requestId") Integer requestId,
                                 @RequestParam("field") String field,
                                 @RequestParam("type") String type, RedirectAttributes redirectAttributes) {
        RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        log.info(requestDTO.toString());
        if(!requestDTO.getApprovalName().equals(user.getUsername())) {

            redirectAttributes.addFlashAttribute("message", "You're not authorized to approve this request");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/request";
        }

        if ("Hạch toán lương".equals(type)) {
            switch (field) {
                case "Approved":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Approved");
                        requestDTO.setApprovalName(user.getUsername());
                        requestService.updateStatus(requestDTO, type);
                    }
                    break;
                case "Denied":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Denied");
                        requestService.updateStatus(requestDTO, type);
                    }
                    break;
            }

        } else if ("Đơn xin nghỉ".equals(type)) {
            switch (field) {
                case "Approved":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Approved");
                        requestDTO.setApprovalName(user.getUsername());
                        requestService.updateStatus(requestDTO, type);
                    }
                    break;
                case "Denied":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Denied");
                        requestService.updateStatus(requestDTO, type);
                    }
                    break;
            }

            var requests = requestService.getRequests(PageRequest.of(0, 10));
            totalRequests = 0;
            totalPendingRequests = 0;
            totalFinishedRequests = 0;
            for (var request : requests) {
                totalRequests++;
                if (request.getRequestStatus().equals("Pending")) totalPendingRequests++;
                else totalFinishedRequests++;
                requestTypes.add(request.getRequestType());
            }

            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("totalPendingRequests", totalPendingRequests);
            model.addAttribute("totalFinishedRequests", totalFinishedRequests);
            model.addAttribute("requestDTO", requestDTO);
            model.addAttribute("requests", requests);
            model.addAttribute(CONTENT_FRAGMENT, REQUEST_FRAGMENTS);
            return INDEX;
        }
        model.addAttribute(CONTENT_FRAGMENT, REQUEST_FRAGMENTS);
        return INDEX;
    }


    @GetMapping("/create/form")
    public String createRequestForm(Model model) {
        var result = departmentService.getAllDepartments();
        model.addAttribute("departmentList", result);

        var requestDTO = requestService.getAllRequests();
        model.addAttribute("requestDTO", requestDTO);
        model.addAttribute("_csrf", ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getAttribute("_csrf"));
        return "request-create";
    }

    @PostMapping("/create")
    @ResponseBody
    public List<RequestCreationResponseDTO> createRequest(@RequestBody RequestCreationRequestDTO request) {
        return requestService.createRequest(request);
    }

    @GetMapping("/create/success")
    public String createRequestSuccess(Model model) {
        var result = departmentService.getAllDepartments();
        model.addAttribute("departmentList", result);

        var requestDTO = requestService.getAllRequests();
        model.addAttribute("requestDTO", requestDTO);
        model.addAttribute("successMessage", "Salary increases processed successfully");
        return "request-create";
    }

    @GetMapping("/view/{id}")
    public String getDetailRequest(@PathVariable Long id, Model model) {
        var result = requestService.getDetailRequest(id);
        model.addAttribute("requestDetail", result);
        return "request-detail";
    }

}
