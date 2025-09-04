package com.example.modfac.controller;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.model.*;
import com.example.modfac.repository.EmployeeRepository;
import com.example.modfac.repository.LeaveRepository;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.EnumMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LeaveControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LeaveRepository leaveRepository;

    private final String API_URL = "/api/leaves";
    private final String ADMIN_USERNAME = "admin1";

    private CaptureLeaveDTO leaveDTO;
    private Employee employee;
    private Employee manager;

    @BeforeEach
    void setUp() {
        // Create manager
        manager = new Employee();
        manager.setFirstName("Manager");
        manager.setLastName("One");
        manager.setPhoneNumber("+10000000001");
        manager.setLeaveInfo(new EnumMap<>(LeaveType.class));
        manager = employeeRepository.save(manager);

        // Create employee with leave balance and assign manager
        employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setPhoneNumber("+12345678901");
        employee.setLeaveInfo(new EnumMap<>(LeaveType.class));
        employee.getLeaveInfo().put(LeaveType.SICK, 10);

        Employee.JobInfo jobInfo = new Employee.JobInfo();
        jobInfo.setEmail("jane.doe@example.com");
        LocalDate now = LocalDate.now();
        jobInfo.setHireDate(now);
        jobInfo.setJobId("DEV123");
        jobInfo.setSalary(60000);
        jobInfo.setManager(manager);
        employee.setJobInfo(jobInfo);

        employee = employeeRepository.save(employee);

        // Prepare a valid leave DTO
        leaveDTO = new CaptureLeaveDTO();
        leaveDTO.setEmployeeId(employee.getId().toString());
        leaveDTO.setLeaveType(LeaveType.SICK);
        leaveDTO.setStartDate(now.plusDays(1));
        leaveDTO.setEndDate(now.plusDays(2));
        leaveDTO.setStatus(Status.PENDING);
        leaveDTO.setApprovedById(manager.getId().toString());
        leaveDTO.setReason("Feeling sick.");
    }

    @AfterEach
    void cleanUp() {
        leaveRepository.deleteAll();
        employeeRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- SUCCESS CASE ---

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenValidInputAndManagerApproves_shouldReturnCreatedLeave() throws Exception {
        // Given
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        // When
        mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveDTO))
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated())
        .andReturn();

    }

    // --- FAILURE: ApprovedBy is not manager ---

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenApprovedByIsNotManager_shouldReturnBadRequest() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        // Approver ID that is NOT the manager
        Employee wrongApprover = new Employee();
        wrongApprover.setFirstName("Another");
        wrongApprover.setLastName("Guy");
        wrongApprover.setPhoneNumber("+12345000000");
        wrongApprover = employeeRepository.save(wrongApprover);

        leaveDTO.setApprovedById(wrongApprover.getId().toString());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // --- FAILURE: Insufficient leave balance ---

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenInsufficientLeaveBalance_shouldReturnBadRequest() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        // Set very long leave period
        LocalDate now = LocalDate.now();
        leaveDTO.setStartDate(now.plusDays(1));
        leaveDTO.setEndDate(now.plusDays(20)); // 20 days

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // --- FAILURE: End date before start date ---

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenEndDateBeforeStartDate_shouldReturnBadRequest() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        LocalDate now = LocalDate.now();
        leaveDTO.setStartDate(now.plusDays(5));
        leaveDTO.setEndDate(now.plusDays(2));

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // --- FAILURE: Validation error (missing fields) ---

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenInvalidDto_shouldReturnValidationError() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        CaptureLeaveDTO invalid = new CaptureLeaveDTO(); // missing fields

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // --- UNAUTHORIZED CASES ---

    @Test
    @WithAnonymousUser
    void requestLeave_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO)))
                .andExpect(status().isUnauthorized());
    }

    // --- Utility Method ---

    private void createAdminUser() {
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword("notHashed");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}

