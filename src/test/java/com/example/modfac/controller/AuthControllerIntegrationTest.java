package com.example.modfac.controller;

import com.example.modfac.dto.LoginDTO;
import com.example.modfac.dto.RegisterUserDTO;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.modfac.config.SecurityConfig.ADMIN_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest extends IntegrationTestSuperclass {
    /**
     * Integration tests for the AuthController class.
     *
     * This class verifies the functionality of authentication and authorization endpoints,
     * including user registration, login, and role-based access control.
     * It uses MockMvc to simulate HTTP requests and validate responses.
     *
     * The tests ensure that the AuthController behaves as expected under various scenarios,
     * such as successful and failed user registration, login, and access control.
     */

    public static final String USER_ROLE = "USER";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterUserDTO registerUserDTO;
    private RegisterUserDTO createAdminDTO;
    private LoginDTO loginDTO;
    private LoginDTO brokenLoginDTO;
    private User user;

    /**
     * Initializes test data before each test case.
     *
     * This method sets up the necessary objects and data required for the tests,
     * including user registration and login DTOs, as well as a user entity.
     */
    void setUp() {
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername(USER_USERNAME);
        registerUserDTO.setPassword("securePassword");
    
        createAdminDTO = new RegisterUserDTO();
        createAdminDTO.setUsername(ADMIN_USERNAME);
        createAdminDTO.setPassword("securePassword");
    
        loginDTO = new LoginDTO();
        loginDTO.setUsername(USER_USERNAME);
        loginDTO.setPassword("securePassword");
    
        brokenLoginDTO = new LoginDTO();
        brokenLoginDTO.setUsername(USER_USERNAME);
        brokenLoginDTO.setPassword("fake");
    
        user = new User();
        user.setUsername(USER_USERNAME);
        user.setPassword("securePassword");
        user.setRole(Role.USER);
    }

    /**
     * Cleans up resources after each test case.
     *
     * This method ensures that any resources or data used during the test
     * are properly released or reset to maintain test isolation and prevent
     * interference between test cases.
     */
    void cleanUp() {
        super.cleanUp();
    }

    /**
     * Tests the creation of an admin user.
     *
     * This test verifies that when an admin user is successfully created,
     * the response contains a success message and the user is present in the repository.
     * It uses a valid admin token to perform the operation.
     */
    void createAdminUser_ShouldReturnSuccessMessage_WhenAdminCreated() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, ADMIN_ROLE);
    
        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin user created successfully"));
    
        assertThat(userRepository.findByUsername(registerUserDTO.getUsername())).isPresent();
    }

    /**
     * Tests the creation of an admin user when the operation is forbidden.
     *
     * This test verifies that attempting to create an admin user with a token
     * associated with a non-admin role results in a forbidden status response.
     */
    void createAdminUser_ShouldReturnError_WhenForbidden() throws Exception {
        createSimpleUser();
        String token = jwtTokenProvider.createToken(USER_USERNAME, USER_ROLE);
    
        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests the creation of an admin user when the request is unauthorized.
     *
     * This test verifies that attempting to create an admin user without providing
     * a valid authorization token results in an unauthorized status response.
     */
    void createAdminUser_ShouldReturnError_WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests the creation of an admin user when the username already exists.
     *
     * This test verifies that attempting to create an admin user with a username
     * that is already present in the repository results in a conflict status response
     * and an appropriate error message.
     */
    void createAdminUser_ShouldReturnErrorMessage_WhenAdminCreationFails() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, ADMIN_ROLE);
    
        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    /**
     * Tests the successful registration of a user.
     *
     * This test verifies that when a user is successfully registered,
     * the response contains a success message and the user is present in the repository.
     * It uses a valid admin token to perform the operation.
     */
    void registerUser_ShouldRunSuccessfully_WhenUserRegistered() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, ADMIN_ROLE);
    
        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
    
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    
        assertThat(userRepository.findByUsername(registerUserDTO.getUsername())).isPresent();
    }

    /**
     * Tests the registration of a user when the username already exists.
     *
     * This test verifies that attempting to register a user with a username
     * that is already present in the repository results in a conflict status response
     * and an appropriate error message.
     */
    void registerUser_ShouldReturnErrorMessage_WhenRegistrationFails() throws Exception {
        createSimpleUser();
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, ADMIN_ROLE);
    
        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    /**
     * Tests the registration of a user when the request is unauthorized.
     *
     * This test verifies that attempting to register a user without providing
     * a valid authorization token results in an unauthorized status response.
     */
    void registerUser_ShouldReturnError_WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests the registration of a user when the operation is forbidden.
     *
     * This test verifies that attempting to register a user with a token
     * associated with a non-admin role results in a forbidden status response.
     */
    void registerUser_ShouldReturnError_WhenForbidden() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(USER_USERNAME, USER_ROLE);
    
        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests the successful authentication of a user and JWT token generation.
     *
     * This test verifies that when a user logs in with valid credentials,
     * the response contains a valid JWT token, the correct username, and the user's role.
     */
    void authenticateUser_ShouldReturnJwtToken_WhenLoginSuccessful() throws Exception {
        createSimpleUser();
    
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(loginDTO.getUsername()))
                .andExpect(jsonPath("$.role").value(Role.USER.name()));
    }

    /**
     * Tests the authentication of a user when login fails due to invalid credentials.
     *
     * This test verifies that attempting to log in with incorrect username or password
     * results in an unauthorized status response and an appropriate error message.
     */
    void authenticateUser_ShouldReturnUnauthorized_WhenLoginFails() throws Exception {
        createSimpleUser();
    
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brokenLoginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}