package com.example.modfac.controller;

import com.example.modfac.dto.LoginDTO;
import com.example.modfac.dto.RegisterUserDTO;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.AuthController;
import com.example.modfac.security.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private RegisterUserDTO registerUserDTO;
    private LoginDTO loginDTO;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername("testUser");
        registerUserDTO.setPassword("securePassword");

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("securePassword");

        user = new User();
        user.setUsername("testUser");
        user.setPassword("securePassword");
        user.setRole(Role.USER);
    }

    @Test
    void registerUser_ShouldReturnSuccessMessage_WhenUserRegistered() throws Exception {
        when(authService.createUser(any(RegisterUserDTO.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(authService, times(1)).createUser(any(RegisterUserDTO.class));
    }

    @Test
    void registerUser_ShouldReturnErrorMessage_WhenRegistrationFails() throws Exception {
        when(authService.createUser(any(RegisterUserDTO.class))).thenThrow(new RuntimeException("Registration failed"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration failed"));

        verify(authService, times(1)).createUser(any(RegisterUserDTO.class));
    }

    @Test
    void authenticateUser_ShouldReturnJwtToken_WhenLoginSuccessful() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenReturn("mockJwtToken");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockJwtToken"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    void authenticateUser_ShouldReturnUnauthorized_WhenLoginFails() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    void createAdminUser_ShouldReturnSuccessMessage_WhenAdminCreated() throws Exception {
        when(authService.createAdmin(any(RegisterUserDTO.class))).thenReturn(user);

        mockMvc.perform(post("/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin user created successfully"));

        verify(authService, times(1)).createAdmin(any(RegisterUserDTO.class));
    }

    @Test
    void createAdminUser_ShouldReturnErrorMessage_WhenAdminCreationFails() throws Exception {
        when(authService.createAdmin(any(RegisterUserDTO.class))).thenThrow(new RuntimeException("Admin creation failed"));

        mockMvc.perform(post("/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Admin creation failed"));

        verify(authService, times(1)).createAdmin(any(RegisterUserDTO.class));
    }

}