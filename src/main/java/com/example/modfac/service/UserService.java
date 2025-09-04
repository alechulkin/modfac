package com.example.modfac.service;

import com.example.modfac.exception.UnauthorizedException;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    public static final int NUM_USERS_TO_ADD = 10;
    public static final int NUM_ADMINS_TO_ADD = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void generateUsers() {
        // Add Users
        addUsersWithRole(Role.USER, "user", "password", NUM_USERS_TO_ADD);

        // Add Admins
        addUsersWithRole(Role.ADMIN, "admin", "adminpass", NUM_ADMINS_TO_ADD);

        log.info("Database initialization finished.");
        log.info("Total users in DB now: {}", userRepository.count());
    }

    public void verifyAdminUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (Role.ADMIN != user.getRole()) {
            log.warn("Unauthorized attempt to create employee by user: {}", username);
            throw new UnauthorizedException("Only admin users can create employees");
        }
    }

    private void addUsersWithRole(Role role, String usernamePrefix, String passwordPrefix, int count) {
        log.info("Attempting to add {} users with role {}", count, role);
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
                    log.info("Added {}/{} {}s...", addedCount, count, role.name().toLowerCase());
                }
            } catch (DuplicateKeyException e) {
                log.warn("Duplicate key error for username '{}'. Skipping.", username);
                skippedCount++;
            } catch (Exception e) {
                log.error("Error saving user '{}' with role {}: {}", username, role, e.getMessage(), e);
            }
        }
        log.info("Finished adding {}s. Added: {}, Skipped (already existed): {}", role.name().toLowerCase(), addedCount,
                skippedCount);
    }
}
