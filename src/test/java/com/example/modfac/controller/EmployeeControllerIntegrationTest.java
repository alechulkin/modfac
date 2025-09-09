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
    private OnboardEmployeeDTO validDto;
    private final String API_URL = "/api/employees";

    @BeforeEach
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

    @AfterEach
    void cleanUp() {
        super.cleanUp();
    }

    // --- Success Case ---

    @Test
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

    @Test
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

    @Test
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

    @Test
    void onboardEmployee_whenUserRoleAndTokenNotProvided_shouldReturn401Error() throws Exception {
        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isUnauthorized()); // Expect HTTP 401 Unauthorized
    }

    // --- Validation Failure Case ---

    @Test
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