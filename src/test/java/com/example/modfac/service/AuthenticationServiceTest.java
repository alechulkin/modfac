package com.example.modfac.service;

import com.example.modfac.dto.LoginDTO;
import com.example.modfac.dto.RegisterUserDTO;
import com.example.modfac.exception.ResourceNotFoundException;
import com.example.modfac.exception.UsernameAlreadyExistsException;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.AuthenticationService;
import com.example.modfac.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterUserDTO registerUserDTO;
    private User user, admin;

    @BeforeEach
    void setUp() {
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername("testUser");
        registerUserDTO.setPassword("password123");

        user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedUserPassword");
        user.setRole(Role.USER);

        admin = new User();
        admin.setUsername("testAdmin");
        admin.setPassword("encodedAdminPassword");
        admin.setRole(Role.ADMIN);
    }

    @Test
    void createUser_ShouldSaveNewUser() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = authenticationService.createUser(registerUserDTO);

        assertNotNull(createdUser);
        assertEquals("testUser", createdUser.getUsername());
        assertEquals(Role.USER, createdUser.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createAdmin_ShouldSaveNewAdminUser() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(admin);

        User createdAdmin = authenticationService.createAdmin(registerUserDTO);

        assertNotNull(createdAdmin);
        assertEquals("testAdmin", createdAdmin.getUsername());
        assertEquals(Role.ADMIN, createdAdmin.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.of(user));

        assertThrows(UsernameAlreadyExistsException.class, () -> authenticationService.createUser(registerUserDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(user.getUsername(), user.getRole().toString())).thenReturn("mockJwtToken");

        String token = authenticationService.login(loginDTO);

        assertNotNull(token);
        assertEquals("mockJwtToken", token);
    }

    @Test
    void login_ShouldThrowException_WhenUsernameIsInvalid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("invalidUser");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("wrongPassword");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        User foundUser = authenticationService.getCurrentUser("testUser");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authenticationService.getCurrentUser("nonExistentUser"));
    }
}