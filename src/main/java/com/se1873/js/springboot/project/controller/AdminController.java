package com.se1873.js.springboot.project.controller;

import com.se1873.js.springboot.project.dto.AuditLogDTO;
import com.se1873.js.springboot.project.dto.UserDTO;
import com.se1873.js.springboot.project.entity.AuditLog;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.repository.RoleRepository;
import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.mapper.UserDTOMapper;
import com.se1873.js.springboot.project.repository.AuditLogRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.AuditLogService;
import com.se1873.js.springboot.project.service.role.RoleService;
import com.se1873.js.springboot.project.service.user.UserService;
import com.se1873.js.springboot.project.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
  private final RoleRepository roleRepository;
  private final UserService userService;
  private final AuditLogService auditLogService;
  private final GlobalController globalController;
  private final UserRepository userRepository;
  private final AuditLogRepository auditLogRepository;
  private final EntityManager entityManager;
  private final RoleService roleService;

  @GetMapping("/roles")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String roles(Model model, @RequestParam(required = false) String message) {
    if (message != null) {
      model.addAttribute("message", message);
      model.addAttribute("messageType", "success");
    }
    model.addAttribute("roles", roleService.findAll());
    model.addAttribute("contentFragment", "fragments/role-management-fragments");
    return "index";
  }

  @GetMapping("/users")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String listUsers(Model model,
                          @ModelAttribute("loggedInUser") User loggedInUser,
                          @RequestParam(required = false) String query,
                          @RequestParam(required = false) String role,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false, defaultValue = "username") String sort,
                          @RequestParam(required = false, defaultValue = "asc") String dir,
                          @PageableDefault(size = 10) Pageable pageable) {

    globalController.createAuditLog(loggedInUser, "View user management page", "View", "Normal");

    Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable customPageable = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      Sort.by(direction, sort)
    );

    Page<UserDTO> users;
    if (query != null && !query.isEmpty()) {
      users = userService.searchUsers(query, customPageable);
    } else if (role != null && !role.isEmpty()) {
      users = userService.findByRole(role, customPageable);
    } else if (status != null && !status.isEmpty()) {
      users = userService.findByStatus(status, customPageable);
    } else {
      users = userService.getAll(customPageable);
    }

    log.error(users.getContent().toString());

    int totalUsers = userService.countAllUsers();
    int activeUsers = userService.countByStatus("Active");
    int lockedUsers = userService.countByStatus("locked");
    var roles = roleService.findAll().values();
    int totalRoles = roles.size();

    Map<Integer, String> userInitials = users.stream()
      .collect(Collectors.toMap(UserDTO::getUserId, u -> StringUtils.getUserInitial(u.getUsername())));

    Map<Integer, String> userFullNames = users.stream()
      .collect(Collectors.toMap(UserDTO::getUserId,
        u -> StringUtils.formatFullName(u.getEmployeeFirstName(), u.getEmployeeLastName())));

    model.addAttribute("users", users);
    model.addAttribute("userInitials", userInitials);
    model.addAttribute("userFullNames", userFullNames);
    model.addAttribute("roles", roles);
    model.addAttribute("totalUsers", totalUsers);
    model.addAttribute("activeUsers", activeUsers);
    model.addAttribute("lockedUsers", lockedUsers);
    model.addAttribute("totalRoles", totalRoles);
    model.addAttribute("contentFragment", "fragments/admin-users-fragments");
    return "index";
  }

  @GetMapping("/users/search")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String searchUsers(@RequestParam String query,
                            @RequestParam(required = false, defaultValue = "username") String sort,
                            @RequestParam(required = false, defaultValue = "asc") String dir,
                            @PageableDefault(size = 10) Pageable pageable) {
    return "redirect:/admin/users?query=" + query + "&sort=" + sort + "&dir=" + dir;
  }

  @GetMapping("/users/{id}/edit")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String showEditUserForm(@PathVariable Integer id,
                                 Model model,
                                 @ModelAttribute("loggedInUser") User loggedInUser,
                                 RedirectAttributes redirectAttributes) {
    try {
      UserDTO user = userService.findByUserId(id);
      if (user == null) {
        redirectAttributes.addFlashAttribute("errorMessage", "User not found");
        return "redirect:/admin/users";
      }

      globalController.createAuditLog(loggedInUser, "View edit user form for: " + user.getUsername(), "View", "Normal");

      model.addAttribute("user", user);
      model.addAttribute("roles", userService.getAllRoles());
      model.addAttribute("contentFragment", "fragments/admin-users-edit-fragments");

      return "index";
    } catch (Exception e) {
      globalController.createAuditLog(loggedInUser, "Error accessing edit form: " + e.getMessage(), "Error", "High");
      redirectAttributes.addFlashAttribute("errorMessage", "Error accessing edit form: " + e.getMessage());
      return "redirect:/admin/users";
    }
  }

  @PostMapping("/users/{id}/edit")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String updateUser(@PathVariable Integer id,
                           @ModelAttribute UserDTO userDTO,
                           @ModelAttribute("loggedInUser") User loggedInUser,
                           RedirectAttributes redirectAttributes) {
    try {
      entityManager.detach(loggedInUser);

      userDTO.setUserId(id);
      userService.updateUser(userDTO);

      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog auditLog = new AuditLog();
      auditLog.setUser(detachedUserRef);
      auditLog.setActionInfo("Updated user: " + userDTO.getUsername());
      auditLog.setActionType("Update");
      auditLog.setActionLevel("Normal");
      auditLogRepository.save(auditLog);

      redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
    } catch (Exception e) {
      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog errorLog = new AuditLog();
      errorLog.setUser(detachedUserRef);
      errorLog.setActionInfo("Error updating user: " + e.getMessage());
      errorLog.setActionType("Error");
      errorLog.setActionLevel("High");
      auditLogRepository.save(errorLog);

      redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
    }

    return "redirect:/admin/users";
  }

  @PostMapping("/users/lock")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String lockUser(@RequestParam Integer userId,
                         @RequestParam(required = false) String reason,
                         @ModelAttribute("loggedInUser") User loggedInUser,
                         RedirectAttributes redirectAttributes) {
    try {
      entityManager.detach(loggedInUser);

      User userToLock = userRepository.findUserByUserId(userId);

      userToLock.setStatus("locked");
      userRepository.save(userToLock);

      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog auditLog = new AuditLog();
      auditLog.setUser(detachedUserRef);
      auditLog.setActionInfo("Locked user: " + userToLock.getUsername());
      auditLog.setActionType("Update");
      auditLog.setActionLevel("High");
      auditLogRepository.save(auditLog);

      redirectAttributes.addFlashAttribute("successMessage", "User account locked successfully");
    } catch (Exception e) {
      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog errorLog = new AuditLog();
      errorLog.setUser(detachedUserRef);
      errorLog.setActionInfo("Error locking user: " + e.getMessage());
      errorLog.setActionType("Error");
      errorLog.setActionLevel("High");
      auditLogRepository.save(errorLog);

      redirectAttributes.addFlashAttribute("errorMessage", "Error locking user: " + e.getMessage());
    }

    return "redirect:/admin/users";
  }

  @PostMapping("/users/unlock")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String unlockUser(@RequestParam Integer userId,
                           @ModelAttribute("loggedInUser") User loggedInUser,
                           RedirectAttributes redirectAttributes) {
    try {
      entityManager.detach(loggedInUser);

      User userToUnlock = userRepository.findUserByUserId(userId);

      userToUnlock.setStatus("Active");
      userRepository.save(userToUnlock);

      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog auditLog = new AuditLog();
      auditLog.setUser(detachedUserRef);
      auditLog.setActionInfo("Unlocked user: " + userToUnlock.getUsername());
      auditLog.setActionType("Update");
      auditLog.setActionLevel("High");
      auditLogRepository.save(auditLog);

      redirectAttributes.addFlashAttribute("successMessage", "User account unlocked successfully");
    } catch (Exception e) {
      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog errorLog = new AuditLog();
      errorLog.setUser(detachedUserRef);
      errorLog.setActionInfo("Error unlocking user: " + e.getMessage());
      errorLog.setActionType("Error");
      errorLog.setActionLevel("High");
      auditLogRepository.save(errorLog);

      redirectAttributes.addFlashAttribute("errorMessage", "Error unlocking user: " + e.getMessage());
    }

    return "redirect:/admin/users";
  }

  @PostMapping("/users/delete")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String deleteUser(@RequestParam Integer userId,
                           @ModelAttribute("loggedInUser") User loggedInUser,
                           RedirectAttributes redirectAttributes) {
    try {
      entityManager.detach(loggedInUser);

      User userToDelete = userRepository.findUserByUserId(userId);

      String username = userToDelete.getUsername();
      userRepository.delete(userToDelete);

      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog auditLog = new AuditLog();
      auditLog.setUser(detachedUserRef);
      auditLog.setActionInfo("Deleted user: " + username);
      auditLog.setActionType("Delete");
      auditLog.setActionLevel("High");
      auditLogRepository.save(auditLog);

      redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
    } catch (Exception e) {
      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog errorLog = new AuditLog();
      errorLog.setUser(detachedUserRef);
      errorLog.setActionInfo("Error deleting user: " + e.getMessage());
      errorLog.setActionType("Error");
      errorLog.setActionLevel("High");
      auditLogRepository.save(errorLog);

      redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
    }

    return "redirect:/admin/users";
  }

  @PostMapping("/users/change-role")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String changeUserRole(@RequestParam Integer userId,
                             @RequestParam String role,
                             @ModelAttribute("loggedInUser") User loggedInUser,
                             RedirectAttributes redirectAttributes) {
    try {
      entityManager.detach(loggedInUser);

      User userToUpdate = userRepository.findUserByUserId(userId);
      String oldRole = userToUpdate.getRole();

      Role newRole = roleRepository.getRoleByName(role);
      userToUpdate.setRole(newRole.getName());
      userRepository.save(userToUpdate);

      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog auditLog = new AuditLog();
      auditLog.setUser(detachedUserRef);
      auditLog.setActionInfo("Changed role for user: " + userToUpdate.getUsername() + " from " + oldRole + " to " + role);
      auditLog.setActionType("Update");
      auditLog.setActionLevel("High");
      auditLogRepository.save(auditLog);

      redirectAttributes.addFlashAttribute("successMessage", "User role changed successfully");
    } catch (Exception e) {
      User detachedUserRef = new User();
      detachedUserRef.setUserId(loggedInUser.getUserId());

      AuditLog errorLog = new AuditLog();
      errorLog.setUser(detachedUserRef);
      errorLog.setActionInfo("Error changing user role: " + e.getMessage());
      errorLog.setActionType("Error");
      errorLog.setActionLevel("High");
      auditLogRepository.save(errorLog);

      redirectAttributes.addFlashAttribute("errorMessage", "Error changing user role: " + e.getMessage());
    }

    return "redirect:/admin/users";
  }

  @GetMapping("/logs")
  @PreAuthorize("hasPermission('SYSTEM', 'VISIBLE')")
  public String logs(Model model,
                     @ModelAttribute("loggedInUser") User loggedInUser,
                     @RequestParam(required = false) String query,
                     @RequestParam(required = false) String actionType,
                     @RequestParam(required = false) String level,
                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                     @RequestParam(required = false, defaultValue = "createdAt") String sort,
                     @RequestParam(required = false, defaultValue = "desc") String dir,
                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

    Pageable customPageable = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      Sort.by(direction, sort)
    );

    Page<AuditLogDTO> auditLogs;

    if (query != null && !query.isEmpty()) {
      auditLogs = auditLogService.searchLogs(query, customPageable);
    } else if (actionType != null && !actionType.isEmpty()) {
      auditLogs = auditLogService.findByActionType(actionType, customPageable);
    } else if (level != null && !level.isEmpty()) {
      auditLogs = auditLogService.findByLevel(level, customPageable);
    } else if (date != null) {
      auditLogs = auditLogService.findByDate(date, customPageable);
    } else {
      auditLogs = auditLogService.getAll(customPageable);
    }

    if (query != null && !query.isEmpty() && date != null) {
      Page<AuditLogDTO> dateFilteredLogs = auditLogService.findByDate(date, customPageable);
      List<AuditLogDTO> filteredContent = dateFilteredLogs.getContent().stream()
          .filter(log -> {
            String username = log.getUsername() != null ? log.getUsername().toLowerCase() : "";
            String actionInfo = log.getActionInfo() != null ? log.getActionInfo().toLowerCase() : "";
            String searchQuery = query.toLowerCase();
            return username.contains(searchQuery) || actionInfo.contains(searchQuery);
          })
          .collect(Collectors.toList());
      auditLogs = new PageImpl<>(filteredContent, customPageable, filteredContent.size());
    }

    Map<String, Integer> quantity = auditLogService.getQuantity();

    Map<String, Object> activityTimeline = new HashMap<>();
    List<String> dates = new ArrayList<>();
    List<Integer> views = new ArrayList<>();
    List<Integer> navigates = new ArrayList<>();
    List<Integer> updates = new ArrayList<>();
    List<Integer> creates = new ArrayList<>();
    List<Integer> errors = new ArrayList<>();

    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

    for (int i = 6; i >= 0; i--) {
      LocalDate currentDate = today.minusDays(i);
      String formattedDate = currentDate.format(formatter);
      dates.add(formattedDate);

      int viewCount = auditLogService.countByActionTypeAndDate("View", currentDate);
      int navigateCount = auditLogService.countByActionTypeAndDate("Navigate", currentDate);
      int updateCount = auditLogService.countByActionTypeAndDate("Update", currentDate);
      int createCount = auditLogService.countByActionTypeAndDate("Create", currentDate);
      int errorCount = auditLogService.countByActionTypeAndDate("Error", currentDate);

      views.add(viewCount);
      navigates.add(navigateCount);
      updates.add(updateCount);
      creates.add(createCount);
      errors.add(errorCount);
    }

    activityTimeline.put("dates", String.join(",", dates));
    activityTimeline.put("views", views.stream().map(String::valueOf).collect(Collectors.joining(",")));
    activityTimeline.put("navigates", navigates.stream().map(String::valueOf).collect(Collectors.joining(",")));
    activityTimeline.put("updates", updates.stream().map(String::valueOf).collect(Collectors.joining(",")));
    activityTimeline.put("creates", creates.stream().map(String::valueOf).collect(Collectors.joining(",")));
    activityTimeline.put("errors", errors.stream().map(String::valueOf).collect(Collectors.joining(",")));

    model.addAttribute("activityTimeline", activityTimeline);
    model.addAttribute("quantity", quantity);
    model.addAttribute("auditLogs", auditLogs);
    model.addAttribute("contentFragment", "fragments/admin-logs-fragments");
    return "index";
  }
}
