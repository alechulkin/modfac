package com.example.modfac.controller;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.EnumMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
class LeaveControllerIntegrationTest extends IntegrationTestSuperclass {
    /**
     * Integration tests for the LeaveController class.
     * 
     * This class verifies the functionality of the LeaveController endpoints by simulating
     * HTTP requests and validating the responses. It includes tests for various scenarios,
     * such as successful leave requests, invalid inputs, and edge cases like insufficient
     * leave balance or incorrect approver information.
     * 
     * The tests use a mock user with ADMIN authority to simulate authenticated requests.
     * The setup and cleanup methods ensure a consistent test environment by preparing
     * necessary data and cleaning up after each test.
     */
    private final String API_URL = "/api/leaves";

    private CaptureLeaveDTO leaveDTO;
    private Employee employee;
    private Employee manager;

    /**
     * Initializes test data before each test case.
     * 
     * This method sets up the necessary test environment by creating and saving
     * a manager and an employee with predefined attributes. It also prepares
     * a valid CaptureLeaveDTO object to be used in the test cases.
     */
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

    /**
     * Cleans up resources after each test case.
     * 
     * This method ensures that the test environment is reset to a clean state
     * by invoking the cleanup logic from the superclass. It is executed after
     * each test case to remove any residual data or configurations that might
     * interfere with subsequent tests.
     */
    void cleanUp() {
        super.cleanUp();
    }

    // --- SUCCESS CASE ---

    /**
     * Tests the scenario where a leave request is successfully created.
     * 
     * This test verifies that when valid input is provided and the manager approves
     * the leave request, the system responds with a status of 201 (Created).
     * It ensures that the leave request is processed correctly under normal conditions.
     */
    void requestLeave_whenValidInputAndManagerApproves_shouldReturnCreatedLeave() throws Exception {
        // Given
        createAdminUser();
    
        // When
        mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveDTO)))
        .andExpect(status().isCreated())
        .andReturn();
    }

    // --- FAILURE: ApprovedBy is not manager ---

    /**
     * Tests the failure scenario when the approver is not the manager.
     * 
     * This test verifies that if the approver ID provided in the leave request
     * does not match the manager's ID, the system responds with a status of 400 (Bad Request).
     * It ensures that leave requests are only approved by the designated manager.
     */
    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenApprovedByIsNotManager_shouldReturnBadRequest() throws Exception {
        createAdminUser();
    
        // Approver ID that is NOT the manager
        Employee wrongApprover = new Employee();
        wrongApprover.setFirstName("Another");
        wrongApprover.setLastName("Guy");
        wrongApprover.setPhoneNumber("+12345000000");
        wrongApprover = employeeRepository.save(wrongApprover);
    
        leaveDTO.setApprovedById(wrongApprover.getId().toString());
    
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO)))
                .andExpect(status().isBadRequest());
    }

    // --- FAILURE: Insufficient leave balance ---

    /**
     * Tests the failure scenario when the leave balance is insufficient.
     * 
     * This test verifies that if the leave request exceeds the available leave balance,
     * the system responds with a status of 400 (Bad Request). It ensures that leave
     * requests are validated against the employee's leave balance.
     */
    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenInsufficientLeaveBalance_shouldReturnBadRequest() throws Exception {
        createAdminUser();
    
        // Set very long leave period
        LocalDate now = LocalDate.now();
        leaveDTO.setStartDate(now.plusDays(1));
        leaveDTO.setEndDate(now.plusDays(20)); // 20 days
    
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO)))
                .andExpect(status().isBadRequest());
    }

    // --- FAILURE: End date before start date ---

    /**
     * Tests the failure scenario when the end date is before the start date.
     * 
     * This test verifies that if the end date provided in the leave request
     * is earlier than the start date, the system responds with a status of 400 (Bad Request).
     * It ensures that leave requests are validated for logical date ranges.
     */
    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenEndDateBeforeStartDate_shouldReturnBadRequest() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");
    
        LocalDate now = LocalDate.now();
        leaveDTO.setStartDate(now.plusDays(5));
        leaveDTO.setEndDate(now.plusDays(2));
    
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDTO)))
                .andExpect(status().isBadRequest());
    }

    // --- FAILURE: Validation error (missing fields) ---

    /**
     * Tests the failure scenario when the DTO is invalid.
     * 
     * This test verifies that if the leave request DTO is missing required fields
     * or contains invalid data, the system responds with a status of 400 (Bad Request).
     * It ensures that the input validation logic is functioning correctly.
     */
    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void requestLeave_whenInvalidDto_shouldReturnValidationError() throws Exception {
        createAdminUser();
    
        CaptureLeaveDTO invalid = new CaptureLeaveDTO(); // missing fields
    
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

}

