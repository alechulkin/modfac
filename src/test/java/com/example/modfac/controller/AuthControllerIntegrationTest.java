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

    @BeforeEach
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

    @AfterEach
    void cleanUp() {
        super.cleanUp();
    }

    @Test
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

    @Test
    void createAdminUser_ShouldReturnError_WhenForbidden() throws Exception {
        createSimpleUser();
        String token = jwtTokenProvider.createToken(USER_USERNAME, USER_ROLE);

        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAdminUser_ShouldReturnError_WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAdminUser_ShouldReturnErrorMessage_WhenAdminCreationFails() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, ADMIN_ROLE);

        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAdminDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
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

    @Test
    void registerUser_ShouldReturnErrorMessage_WhenRegistrationFails() throws Exception {
        createSimpleUser();
        createAdminUser();
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, ADMIN_ROLE);

        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void registerUser_ShouldReturnError_WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUser_ShouldReturnError_WhenForbidden() throws Exception {
        createAdminUser();
        String token = jwtTokenProvider.createToken(USER_USERNAME, USER_ROLE);

        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
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

    @Test
    void authenticateUser_ShouldReturnUnauthorized_WhenLoginFails() throws Exception {
        createSimpleUser();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brokenLoginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}