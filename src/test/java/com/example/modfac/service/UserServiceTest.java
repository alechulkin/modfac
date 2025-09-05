package com.example.modfac.service;

import com.example.modfac.dto.LoginDTO;
import com.example.modfac.dto.RegisterUserDTO;
import com.example.modfac.exception.ResourceNotFoundException;
import com.example.modfac.exception.UnauthorizedException;
import com.example.modfac.exception.UsernameAlreadyExistsException;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User regularUser;

    private RegisterUserDTO registerUserDTO;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUsername("admin1");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setUsername("user1");
        regularUser.setRole(Role.USER);

        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername("testUser");
        registerUserDTO.setPassword("password123");
    }

    // ========== GENERATE USERS TESTS ==========

    @Test
    void generateUsers_ShouldEncodePassword() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        userService.generateUsers();

        // Assert
        verify(passwordEncoder, times(UserService.NUM_USERS_TO_ADD +
                UserService.NUM_ADMINS_TO_ADD)).encode(any());
    }

    // ========== GENERATE USERS TESTS ==========

    @Test
    void generateUsers_ShouldCreateUsersWhenNoneExist() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        userService.generateUsers();

        // Assert
        verify(userRepository, times(UserService.NUM_USERS_TO_ADD +
                UserService.NUM_ADMINS_TO_ADD)).save(any(User.class));
    }

    @Test
    void generateUsers_ShouldHandleDuplicateUsersGracefully() {
        // Act & Assert
        assertDoesNotThrow(() -> userService.generateUsers());

        // Verify interactions
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    // ========== VERIFY ADMIN USER TESTS ==========

    @Test
    void verifyAdminUser_ShouldPassForAdminUser() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertDoesNotThrow(() -> userService.verifyAdminUser("admin1"));
    }

    @Test
    void verifyAdminUser_ShouldThrowUnauthoriezedErrorForRegularUser() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(regularUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> userService.verifyAdminUser("user1"));
    }

    @Test
    void verifyAdminUser_ShouldThrowUnauthorizedErrorWhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> userService.verifyAdminUser("nonexistent"));
    }

    @Test
    void createUser_ShouldSaveNewUser() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        User createdUser = userService.createUser(registerUserDTO);

        assertNotNull(createdUser);
        assertEquals(regularUser.getUsername(), createdUser.getUsername());
        assertEquals(Role.USER, createdUser.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createAdmin_ShouldSaveNewAdminUser() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User createdAdmin = userService.createAdmin(registerUserDTO);

        assertNotNull(createdAdmin);
        assertEquals(adminUser.getUsername(), createdAdmin.getUsername());
        assertEquals(Role.ADMIN, createdAdmin.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.of(regularUser));

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(registerUserDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), regularUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(regularUser.getUsername(), regularUser.getRole().toString())).thenReturn("mockJwtToken");

        String token = userService.login(loginDTO);

        assertNotNull(token);
        assertEquals("mockJwtToken", token);
    }

    @Test
    void login_ShouldThrowException_WhenUsernameIsInvalid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("invalidUser");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.login(loginDTO));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("wrongPassword");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), regularUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(loginDTO));
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(regularUser));

        User foundUser = userService.getCurrentUser("testUser");

        assertNotNull(foundUser);
        assertEquals(regularUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser("nonExistentUser"));
    }


}