package com.example.modfac.service;

import com.example.modfac.exception.UnauthorizedException;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
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

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUsername("admin1");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setUsername("user1");
        regularUser.setRole(Role.USER);
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


}