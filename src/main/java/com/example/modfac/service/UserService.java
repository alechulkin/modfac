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
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserService.class);
    public static final int NUM_USERS_TO_ADD = 10;
    public static final int NUM_ADMINS_TO_ADD = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void generateUsers() {
        LOG.debug("generateUsers method invoked");
    
        // Add Users
        addUsersWithRole(Role.USER, "user", "password", NUM_USERS_TO_ADD);
    
        // Add Admins
        addUsersWithRole(Role.ADMIN, "admin", "adminpass", NUM_ADMINS_TO_ADD);
    
        LOG.info("Database initialization finished.");
        LOG.info("Total users in DB now: {}", userRepository.count());
    
        LOG.debug("generateUsers method finished");
    }

    public void verifyAdminUser(String username) {
        LOG.debug("verifyAdminUser method invoked");
    
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (Role.ADMIN != user.getRole()) {
            LOG.warn("Unauthorized attempt to create employee by user: {}", username);
            throw new UnauthorizedException("Only admin users can create employees");
        }
    
        LOG.debug("verifyAdminUser method finished");
    }

    /**
     * Create a new user
     */
    /**
     * Create a new user
     */
    @Transactional
    public User createUser(RegisterUserDTO registerDto) {
        LOG.debug("createUser method invoked");
    
        String username = registerDto.getUsername();
        LOG.info("Creating user with username: {}", username);
    
        User createdUser = registerUser(registerDto, Role.USER);
    
        LOG.debug("createUser method finished");
        return createdUser;
    }

    /**
     * Create an admin user
     */
    /**
     * Create an admin user
     */
    @Transactional
    public User createAdmin(RegisterUserDTO registerDto) {
        LOG.debug("createAdmin method invoked");
    
        LOG.info("Creating admin user with username: {}", registerDto.getUsername());
    
        User createdAdmin = registerUser(registerDto, Role.ADMIN);
    
        LOG.debug("createAdmin method finished");
        return createdAdmin;
    }

    private User registerUser(RegisterUserDTO registerDto, Role role) {
        LOG.debug("registerUser method invoked");
    
        String username = registerDto.getUsername();
        Optional<User> found = userRepository.findByUsername(username);
    
        if (found.isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
    
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(role);
    
        User savedUser = userRepository.save(user);
    
        LOG.debug("registerUser method finished");
        return savedUser;
    }

    /**
     * Authenticate a user and return JWT token
     */
    /**
     * Authenticate a user and return JWT token
     */
    public String login(LoginDTO loginDto) {
        LOG.debug("login method invoked");
    
        LOG.info("Authenticating user: {}", loginDto.getUsername());
    
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
    
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
    
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole().toString());
    
        LOG.debug("login method finished");
        return token;
    }

    /**
     * Get current user information
     */
    /**
     * Get current user information
     */
    public User getCurrentUser(String username) {
        LOG.debug("getCurrentUser method invoked");
    
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
        LOG.debug("getCurrentUser method finished");
        return user;
    }

    private void addUsersWithRole(Role role, String usernamePrefix, String passwordPrefix, int count) {
        LOG.debug("addUsersWithRole method invoked");
    
        LOG.info("Attempting to add {} users with role {}", count, role);
        int addedCount = 0;
        int skippedCount = 0;
    
        for (int i = 1; i <= count; i++) {
            User user = new User();
            String username = usernamePrefix + i;
            user.setUsername(username);
            String rawPassword = passwordPrefix + i;
            String encodedPassword = passwordEncoder.encode(rawPassword);
            user.setPassword(encodedPassword);
            user.setRole(role);
    
            try {
                userRepository.save(user);
                addedCount++;
                if (addedCount % 20 == 0) { // Log progress periodically
                    LOG.info("Added {}/{} {}s...", addedCount, count, role.name().toLowerCase());
                }
            } catch (DuplicateKeyException e) {
                LOG.warn("Duplicate key error for username '{}'. Skipping.", username);
                skippedCount++;
            } catch (Exception e) {
                LOG.error("Error saving user '{}' with role {}: {}", username, role, e.getMessage(), e);
            }
        }
        LOG.info("Finished adding {}s. Added: {}, Skipped (already existed): {}", role.name().toLowerCase(), addedCount,
                skippedCount);
    
        LOG.debug("addUsersWithRole method finished");
    }
}