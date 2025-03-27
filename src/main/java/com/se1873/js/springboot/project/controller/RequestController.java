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
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("requesters", userRepository.findAll());
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
                       @RequestParam("LeavePolicyId") Integer leavePolicyId,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("contentFragment", "fragments/user-request-create-fragments");
            return "index";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<User> user = userRepository.findUserByUsername(name);
        int employeeId = user.get().getEmployee().getEmployeeId();
        LeavePolicy selectedPolicy = leavePolicyRepository.findLeavePolicyByLeavePolicyId(leavePolicyId);

        if (selectedPolicy != null) {
            requestDTO.getLeaveDTO().setReason(selectedPolicy.getLeavePolicyName());
        }
        boolean isDuplicate = leaveRepository.existsByEmployee_EmployeeIdAndLeavePolicyIdAndStartDateOrEndDate(
                employeeId,
                leavePolicyId,
                requestDTO.getLeaveDTO().getStartDate(),
                requestDTO.getLeaveDTO().getEndDate()
        );

        if (isDuplicate) {
            model.addAttribute("errorMessage", "Đã có yêu cầu nghỉ phép cùng loại và cùng ngày bắt đầu hoặc kết thúc!");
            model.addAttribute("requestDTO", requestDTO);
            model.addAttribute("leavePolicy", leavePolicyRepository.findAll());
            model.addAttribute("reason", leavePolicyId);
            model.addAttribute("contentFragment", "fragments/user-request-create-fragments");
            return "index";
        }

        if (requestDTO.getLeaveDTO().getTotalDays() > requestDTO.getLeaveDTO().getLeaveAllowedDay()) {
            model.addAttribute("errorMessage", "Số ngày nghỉ vượt quá số ngày được cho phép!");
            model.addAttribute("requestDTO", requestDTO);
            model.addAttribute("leavePolicy", leavePolicyRepository.findAll());
            model.addAttribute("reason", leavePolicyId);
            model.addAttribute("contentFragment", "fragments/user-request-create-fragments");
            return "index";
        }

        User admin = userRepository.findUserByUsername("admin").orElseThrow(() ->
                new RuntimeException("Không tìm thấy người dùng"));
        requestDTO.getLeaveDTO().setLeavePolicyId(selectedPolicy.getLeavePolicyId());

        requestService.saveRequestForLeave(requestDTO, user.get(), admin);
        redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu nghỉ phép đã được gửi thành công!");
        return "redirect:/user/detail";
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
                                 @RequestParam("type") String type) {
        RequestDTO requestDTO = requestService.findRequestByRequestId(requestId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        String notificationMessage = "";

        Integer requesterId = requestDTO.getRequesterId();
        User requestUser = userService.findUserByUserId(requesterId);
        Integer employeeId = requestUser.getEmployee().getEmployeeId();
        Employee employee = employeeRepository.findEmployeeByEmployeeId(employeeId);

        if ("Salary Calculation".equals(type)) {
            switch (field) {
                case "Approved":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Approved");
                        requestDTO.setApprovalName(user.getUsername());
                        requestService.updateStatus(requestDTO, type);
                        notificationMessage = "yêu cầu hạch toán lương được phê duyệt bởi " + user.getUsername();
                    }
                    break;
                case "Denied":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Denied");
                        requestService.updateStatus(requestDTO, type);
                        notificationMessage = "yêu cầu hạch toán lương bị từ chối bởi " + user.getUsername();
                    }
                    break;
            }
        } else if ("Leave Permit".equals(type)) {
            switch (field) {
                case "Approved":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Approved");
                        requestDTO.setApprovalName(user.getUsername());
                        requestService.updateStatus(requestDTO,type);
                        int remainingDays = leavePolicyService.calculate(requestDTO.getLeaveDTO().getLeavePolicyId(), employeeId,requestDTO);
                        requestDTO.getLeaveDTO().setLeaveAllowedDay(remainingDays);
                        requestService.save(requestDTO,requestUser,employee);
                        notificationMessage = "đơn xin nghỉ được phê duyệt bởi " + user.getUsername();
                    }
                    break;
                case "Denied":
                    if ("Pending".equals(requestDTO.getRequestStatus())) {
                        requestDTO.setRequestStatus("Denied");
                        requestDTO.setApprovalName(user.getUsername());
                        requestService.updateStatus(requestDTO, type);
                        requestService.save(requestDTO,requestUser,employee);
                        notificationMessage = "đơn xin nghỉ đã bị từ chối bởi " + user.getUsername();
                    }
                    break;
            }
        }
        if (!notificationMessage.isEmpty()) {
            Notification notification = Notification.builder()
                    .user(requestUser)
                    .requestId(requestId)
                    .message(notificationMessage)
                    .status("unread")
                    .createdAt(LocalDateTime.now())
                    .type(type)
                    .build();
            notificationRepository.save(notification);
        }

        var requests = requestService.getRequests(PageRequest.of(0, 10));
        totalRequests = 0;
        totalPendingRequests = 0;
        totalFinishedRequests = 0;
        for (var request : requests) {
            totalRequests++;
            if (request.getRequestStatus().equals("pending")) totalPendingRequests++;
            else totalFinishedRequests++;
            requestTypes.add(request.getRequestType());
        }

        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("totalPendingRequests", totalPendingRequests);
        model.addAttribute("totalFinishedRequests", totalFinishedRequests);
        model.addAttribute("requestDTO", requestDTO);
        model.addAttribute("requests", requests);
        model.addAttribute("contentFragment", "fragments/request-fragments");
        return "index";
    }


    @GetMapping("/create/form")
    public String createRequestForm(Model model) {
        var result = departmentService.getAllDepartments();
        model.addAttribute("departmentList", result);

        var requestDTO = requestService.getAllRequests();
        model.addAttribute("requestDTO", requestDTO);
        model.addAttribute("_csrf", ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getAttribute("_csrf"));
        model.addAttribute("contentFragment", "fragments/request-create-fragments");
      return INDEX;
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

    @PostMapping("/bulk-approve")
    public String bulkApprove(@RequestParam("selectedIds") String selectedIds, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        String[] ids = selectedIds.split(",");
        for (String id : ids) {
            Integer requestId = Integer.parseInt(id);
            Request request = requestRepository.findRequestByRequestId(requestId);

            if (request != null && "Pending".equals(request.getStatus())) {
                if ("Salary Raise".equals(request.getRequestType())) {
                    response responseDetails = reponseRepo.findByRequestId(requestId)
                            .orElseThrow(() -> new RuntimeException("Request not found"));

                    String employeeCode = responseDetails.getEmployeeCode();
                    Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));
                    Contract employeeContract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);
                    employeeContract.setBaseSalary(responseDetails.getNewBaseSalary());
                    contractRepository.save(employeeContract);
                }

                request.setStatus("Approved");
                request.setApproval(user);
                requestRepository.save(request);
            }
        }

        redirectAttributes.addFlashAttribute("message", "Successfully approved selected requests");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/request";
    }

    @PostMapping("/bulk-deny")
    public String bulkDeny(@RequestParam("selectedIds") String selectedIds, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        String[] ids = selectedIds.split(",");
        for (String id : ids) {
            Integer requestId = Integer.parseInt(id);
            Request request = requestRepository.findRequestByRequestId(requestId);

            if (request != null && "Pending".equals(request.getStatus())) {
                if ("Salary Raise".equals(request.getRequestType())) {
                    response responseDetails = reponseRepo.findByRequestId(requestId)
                            .orElseThrow(() -> new RuntimeException("Request not found"));

                    String employeeCode = responseDetails.getEmployeeCode();
                    Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));
                    Contract employeeContract = contractRepository.findContractByEmployee_EmployeeIdAndPresent(employee.getEmployeeId(), true);
                    employeeContract.setBaseSalary(responseDetails.getOldBaseSalary());
                    contractRepository.save(employeeContract);
                }

                request.setStatus("Denied");
                request.setApproval(user);
                requestRepository.save(request);
            }
        }

        redirectAttributes.addFlashAttribute("message", "Successfully denied selected requests");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/request";
    }

    @RequestMapping("/multi-filter")
    public String multiFilter(Model model,
                            @RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "dateRange", required = false) String dateRange,
                            @RequestParam(value = "requester", required = false) String requester,
                            @RequestParam(value = "department", required = false) String department,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "5") int size) {
        Page<RequestDTO> requests = requestService.multiFilter(type, status, dateRange, requester, department, getPageable(page, size));
        return populateRequestModel(model, requests, "request");
    }

}
