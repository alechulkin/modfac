package com.example.modfac.controller;

import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
class EmployeeControllerIntegrationTest extends IntegrationTestSuperclass {
    /**
     * Integration tests for the EmployeeController class.
     * <p>
     * This class verifies the end-to-end behavior of the EmployeeController by simulating HTTP requests
     * and validating the responses. It includes tests for successful employee onboarding, security-related
     * failures, and validation errors.
     * </p>
     * <p>
     * The tests use a mock MVC environment to simulate API calls and validate the controller's behavior
     * under various scenarios, such as valid and invalid input, authentication, and authorization.
     * </p>
     */
    private OnboardEmployeeDTO validDto;
    private final String API_URL = "/api/employees";

    /**
     * Initializes the test environment before each test case.
     * <p>
     * This method sets up a valid OnboardEmployeeDTO object with default values
     * to be used in the test cases. It also ensures that the repository is cleared
     * if not relying solely on @Transactional.
     * </p>
     */
    void setUp() {
        // Clear repository before each test if not relying solely on @Transactional
        // employeeRepository.deleteAll(); // Use with caution or if @Transactional isn't sufficient
    
        validDto = new OnboardEmployeeDTO();
        validDto.setFirstName("John");
        validDto.setLastName("Doe");
        validDto.setStreet("123 Main St");
        validDto.setCity("New York");
        validDto.setState("NY");
        validDto.setZipCode("10001");
        validDto.setPhoneNumber("+1234567890");
        validDto.setEmail("john.doe@example.com");
        validDto.setHireDate(LocalDate.now());
        validDto.setJobId("DEV001");
        validDto.setSalary(80000);
        validDto.setManagerId(new ObjectId().toString());
        // createdBy will be set by the controller using the authenticated user
    }

    /**
     * Cleans up resources or resets the state after each test case.
     * <p>
     * This method ensures that any modifications made during a test case
     * are reverted, maintaining a clean state for subsequent tests.
     * It delegates the cleanup process to the superclass implementation.
     * </p>
     */
    void cleanUp() {
        super.cleanUp();
    }

    // --- Success Case ---

    /**
     * Tests the successful onboarding of an employee by an admin user.
     * <p>
     * This test verifies that when a valid OnboardEmployeeDTO is provided by an admin user,
     * the API responds with HTTP 201 Created and returns the created Employee object.
     * It also checks that the employee is correctly saved in the database.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution.
     */
    void onboardEmployee_whenAdminAndValidDto_shouldReturnCreatedAndEmployee() throws Exception {
        // When
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");
        MvcResult mvcResult = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))
                        .header("Authorization", "Bearer " + token))
    
                // Then
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty()) // Check if ID is generated
                .andExpect(jsonPath("$.firstName").value(validDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(validDto.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(validDto.getPhoneNumber()))
                .andExpect(jsonPath("$.jobInfo.email").value(validDto.getEmail()))
                .andReturn();
    
        // Optional: Verify database state directly
        String responseBody = mvcResult.getResponse().getContentAsString();
        Employee createdEmployee = objectMapper.readValue(responseBody, Employee.class);
        assertThat(employeeRepository.findEmployeeByPhoneNumber(createdEmployee.getPhoneNumber())).isPresent();
    }

    // --- Security Failure Cases ---

    /**
     * Tests the behavior of the API when a regular user attempts to onboard an employee.
     * <p>
     * This test verifies that when a valid OnboardEmployeeDTO is provided by a user with the "USER" role,
     * the API responds with HTTP 403 Forbidden, indicating that the user does not have the necessary permissions
     * to perform the operation.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution.
     */
    void onboardEmployee_whenUserAndValidDto_shouldReturn403Error() throws Exception {
        // When
        String token = jwtTokenProvider.createToken(USER_USERNAME, "USER");
        MvcResult mvcResult = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))
                        .header("Authorization", "Bearer " + token))
    
                // Then
                .andExpect(status().isForbidden()) // Expect HTTP 403
                .andReturn();
    
    }

    /**
     * Tests the behavior of the API when an admin user provides an invalid token.
     * <p>
     * This test verifies that when an invalid token is provided by a user with the "ADMIN" role,
     * the API responds with HTTP 401 Unauthorized, indicating that the token is not valid
     * for authentication.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution.
     */
    void onboardEmployee_whenAdminRoleAndInvalidToken_shouldReturn401Error() throws Exception {
        // When
        String token = "invalidToken";
        MvcResult mvcResult = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))
                        .header("Authorization", "Bearer " + token))
    
                // Then
                .andExpect(status().isUnauthorized()) // Expect HTTP 401 Unauthorized
                .andReturn();
    
    }

    /**
     * Tests the behavior of the API when no user role or token is provided.
     * <p>
     * This test verifies that when a request is made without an authentication token,
     * the API responds with HTTP 401 Unauthorized, indicating that authentication is required.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution.
     */
    void onboardEmployee_whenUserRoleAndTokenNotProvided_shouldReturn401Error() throws Exception {
        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isUnauthorized()); // Expect HTTP 401 Unauthorized
    }

    // --- Validation Failure Case ---

    /**
     * Tests the behavior of the API when an invalid OnboardEmployeeDTO is provided.
     * <p>
     * This test verifies that when an OnboardEmployeeDTO with missing required fields is submitted
     * by an admin user, the API responds with HTTP 400 Bad Request, indicating that the input
     * validation has failed.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution.
     */
    void onboardEmployee_whenInvalidDto_shouldReturnBadRequest() throws Exception {
        // Given
        createAdminUser();
        OnboardEmployeeDTO invalidDto = new OnboardEmployeeDTO(); // Missing required fields
        invalidDto.setLastName("Doe"); // Only set last name
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");
    
        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .header("Authorization", "Bearer " + token))
    
                .andExpect(status().isBadRequest()); // Expect HTTP 400 Bad Request
        // The exact jsonPath depends on your exception handling setup
    }
}