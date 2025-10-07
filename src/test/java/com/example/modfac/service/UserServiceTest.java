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
    /**
     * Unit tests for the UserService class.
     *
     * This class uses JUnit and Mockito to test the functionality of the UserService class.
     * It includes tests for user creation, login, and role verification, among other features.
     * Mock objects are used to isolate the service layer from external dependencies.
     */

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

    /**
     * Initializes test data before each test case.
     *
     * This method sets up mock user data, including an admin user, a regular user,
     * and a RegisterUserDTO object. These objects are used across multiple test cases
     * to ensure consistent and isolated test environments.
     */
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

    /**
     * Tests that the generateUsers method encodes passwords correctly.
     *
     * This test verifies that the PasswordEncoder's encode method is called
     * the expected number of times when generating users. The number of calls
     * is determined by the sum of NUM_USERS_TO_ADD and NUM_ADMINS_TO_ADD constants
     * in the UserService class.
     */
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

    /**
     * Tests that the generateUsers method creates users when none exist.
     *
     * This test verifies that the UserRepository's save method is called
     * the expected number of times when generating users. The number of calls
     * is determined by the sum of NUM_USERS_TO_ADD and NUM_ADMINS_TO_ADD constants
     * in the UserService class.
     */
    void generateUsers_ShouldCreateUsersWhenNoneExist() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(new User());
    
        // Act
        userService.generateUsers();
    
        // Assert
        verify(userRepository, times(UserService.NUM_USERS_TO_ADD +
                UserService.NUM_ADMINS_TO_ADD)).save(any(User.class));
    }

    /**
     * Tests that the generateUsers method handles duplicate users gracefully.
     *
     * This test ensures that no exceptions are thrown when duplicate users
     * are encountered during the user generation process. It also verifies
     * that the UserRepository's save method is called at least once.
     */
    void generateUsers_ShouldHandleDuplicateUsersGracefully() {
        // Act & Assert
        assertDoesNotThrow(() -> userService.generateUsers());
    
        // Verify interactions
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    // ========== VERIFY ADMIN USER TESTS ==========

    /**
     * Tests that the verifyAdminUser method passes for an admin user.
     *
     * This test ensures that no exceptions are thrown when the username
     * corresponds to an admin user in the system.
     */
    void verifyAdminUser_ShouldPassForAdminUser() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
    
        // Act & Assert
        assertDoesNotThrow(() -> userService.verifyAdminUser("admin1"));
    }

    /**
     * Tests that the verifyAdminUser method throws an UnauthorizedException for a regular user.
     *
     * This test ensures that an exception is thrown when the username corresponds
     * to a regular user in the system, as only admin users are authorized.
     */
    void verifyAdminUser_ShouldThrowUnauthoriezedErrorForRegularUser() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(regularUser));
    
        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> userService.verifyAdminUser("user1"));
    }

    /**
     * Tests that the verifyAdminUser method throws an UnauthorizedException when the user is not found.
     *
     * This test ensures that an exception is thrown when the username does not correspond
     * to any user in the system, as only existing admin users are authorized.
     */
    void verifyAdminUser_ShouldThrowUnauthorizedErrorWhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    
        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> userService.verifyAdminUser("nonexistent"));
    }

    /**
     * Tests that the createUser method saves a new user correctly.
     *
     * This test verifies that when a new user is registered, the user is saved
     * in the repository with the correct username and role. It also ensures
     * that the password is encoded before saving.
     */
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

    /**
     * Tests that the createAdmin method saves a new admin user correctly.
     *
     * This test verifies that when a new admin user is registered, the user is saved
     * in the repository with the correct username and role. It also ensures
     * that the password is encoded before saving.
     */
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

    /**
     * Tests that the registerUser method throws a UsernameAlreadyExistsException
     * when attempting to register a user with an existing username.
     *
     * This test ensures that the UserRepository's save method is not called
     * when a duplicate username is detected.
     */
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.of(regularUser));
    
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(registerUserDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests that the login method returns a token when valid credentials are provided.
     *
     * This test verifies that a JWT token is generated and returned when the username
     * and password match an existing user in the system. It ensures that the token
     * contains the correct username and role information.
     */
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

    /**
     * Tests that the login method throws a BadCredentialsException when the username is invalid.
     *
     * This test ensures that an exception is thrown when the username provided
     * does not match any existing user in the system.
     */
    void login_ShouldThrowException_WhenUsernameIsInvalid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("invalidUser");
        loginDTO.setPassword("password123");
    
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());
    
        assertThrows(BadCredentialsException.class, () -> userService.login(loginDTO));
    }

    /**
     * Tests that the login method throws a BadCredentialsException when the password is incorrect.
     *
     * This test ensures that an exception is thrown when the password provided
     * does not match the stored password for the user in the system.
     */
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("wrongPassword");
    
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), regularUser.getPassword())).thenReturn(false);
    
        assertThrows(BadCredentialsException.class, () -> userService.login(loginDTO));
    }

    /**
     * Tests that the getCurrentUser method returns a user when the user exists.
     *
     * This test verifies that the UserRepository's findByUsername method is called
     * and that the returned user matches the expected user when a valid username is provided.
     */
    void getCurrentUser_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(regularUser));
    
        User foundUser = userService.getCurrentUser("testUser");
    
        assertNotNull(foundUser);
        assertEquals(regularUser.getUsername(), foundUser.getUsername());
    }

    /**
     * Tests that the getCurrentUser method throws a ResourceNotFoundException when the user is not found.
     *
     * This test ensures that an exception is thrown when the username provided
     * does not correspond to any existing user in the system.
     */
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser("nonExistentUser"));
    }


}