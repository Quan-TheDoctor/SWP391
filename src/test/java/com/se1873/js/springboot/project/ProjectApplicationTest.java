package com.se1873.js.springboot.project;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.dto.EmployeeDTO;
import com.se1873.js.springboot.project.repository.DepartmentRepository;
import com.se1873.js.springboot.project.repository.PositionRepository;
import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.employee.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectApplicationTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private EmployeeService employeeService;
  @MockitoBean
  private DepartmentRepository departmentRepository;
  @MockitoBean
  private PositionRepository positionRepository;
  @MockitoBean
  private AttendanceService attendanceService;

  @BeforeEach
  void setUp() {
    when(departmentRepository.findAll()).thenReturn(Collections.emptyList());
    when(positionRepository.findAll()).thenReturn(Collections.emptyList());
  }

  @Test
  void shouldRedirectToLoginPageWhenNotAuthenticated() throws Exception {
    this.mockMvc
      .perform(get("/employee/create/form"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("http://localhost/login"));
  }

  @Test
  void shouldFailWithInvalidCredentials() throws Exception {
    this.mockMvc
      .perform(post("/login")
        .param("username", "invaliduser")
        .param("password", "invalidpassword")
        .with(csrf()))
      .andDo(print())
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login?error=true"));
  }

  @Test
  @WithMockUser(username = "quantran", password = "1", roles = "USER")
  void shouldFailWithLackingAuthorities() throws Exception {
    this.mockMvc
      .perform(get("/employee").with(csrf()))
      .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(username = "admin", password = "1", roles = "ADMIN")
  void shouldAccessEmployeePageSuccessfully() throws Exception {
    Page<EmployeeDTO> emptyPage = new PageImpl<>(Collections.emptyList());
    when(employeeService.getAll(any(Pageable.class))).thenReturn(emptyPage);

    this.mockMvc
      .perform(get("/employee").with(csrf()))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "admin", password = "1", roles = "ADMIN")
  void shouldAccessEmployeeViewPageSuccessfully() throws Exception {
    EmployeeDTO employeeDTO = new EmployeeDTO();
    when(employeeService.getEmployeeByEmployeeId(any(Integer.class))).thenReturn(employeeDTO);
    when(departmentRepository.findAll()).thenReturn(Collections.emptyList());
    when(positionRepository.findAll()).thenReturn(Collections.emptyList());

    this.mockMvc.perform(get("/employee/view")
        .param("employeeId", "1"))
      .andExpect(status().isOk())
      .andExpect(view().name("employee-view"))
      .andExpect(model().attributeExists("employeeDTO"))
      .andExpect(model().attributeExists("departments"));
  }

  @Test
  @WithMockUser(username = "admin", password = "1", roles = "ADMIN")
  void shouldAccessAttendancePageSuccessfully() throws Exception {
    Page<AttendanceDTO> emptyPage = new PageImpl<>(Collections.emptyList());
    Map<String, Integer> quantity = new HashMap<>();

    when(attendanceService.getAll(LocalDate.now(), LocalDate.now(), PageRequest.of(0, 5))).thenReturn(emptyPage);
    when(attendanceService.getQuantity()).thenReturn(quantity);

    this.mockMvc.perform(get("/attendance")
        .param("page", "0")
        .param("size", "5")
        .param("startDate", String.valueOf(LocalDate.now()))
        .param("endDate", String.valueOf(LocalDate.now()))
        .with(csrf()))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "annguyen", password = "1", roles = "Administrator")
  void shouldHandleInvalidAttendanceData() throws Exception {
    this.mockMvc.perform(post("/attendance/create")
        .param("employeeId", "invalid")
        .param("date", "invalid-date")
        .param("status", "INVALID_STATUS")
        .with(csrf()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", password = "1", roles = "ADMIN")
  void shouldHandleAttendanceSearchWithInvalidDates() throws Exception {
    this.mockMvc.perform(get("/attendance")
        .param("page", "0")
        .param("size", "5")
        .param("startDate", "invalid-date")
        .param("endDate", "invalid-date")
        .with(csrf()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", password = "1", roles = "ADMIN")
  void shouldHandleAttendanceSearchWithEndDateBeforeStartDate() throws Exception {
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.minusDays(1);

    this.mockMvc.perform(get("/attendance")
        .param("page", "0")
        .param("size", "5")
        .param("startDate", String.valueOf(startDate))
        .param("endDate", String.valueOf(endDate))
        .with(csrf()))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", password = "1", roles = "ADMIN")
  void shouldHandleAttendanceSearchWithPagination() throws Exception {
    Page<AttendanceDTO> attendancePage = new PageImpl<>(Collections.emptyList());
    Map<String, Integer> quantity = new HashMap<>();

    when(attendanceService.getAll(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
      .thenReturn(attendancePage);
    when(attendanceService.getQuantity()).thenReturn(quantity);

    this.mockMvc.perform(get("/attendance")
        .param("page", "1")
        .param("size", "10")
        .param("startDate", String.valueOf(LocalDate.now()))
        .param("endDate", String.valueOf(LocalDate.now()))
        .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(view().name("index"))
      .andExpect(model().attributeExists("attendances"))
      .andExpect(model().attributeExists("quantity"));
  }
}