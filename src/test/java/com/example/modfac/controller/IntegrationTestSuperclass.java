package com.example.modfac.controller;

import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.EmployeeRepository;
import com.example.modfac.repository.LeaveRepository;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
public class IntegrationTestSuperclass {
    /**
     * This class serves as a base class for integration tests, providing common setup and utility methods.
     * It includes pre-configured components such as MockMvc, ObjectMapper, and repositories for testing purposes.
     * Additionally, it provides helper methods to create users with specific roles and clean up test data.
     */
    static final String ADMIN_USERNAME = "admin1";
    static final String USER_USERNAME = "user1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    LeaveRepository leaveRepository;

    /**
         * Creates and returns an admin user with a predefined username and role.
         * The admin user is assigned the username specified by the constant ADMIN_USERNAME
         * and the role Role.ADMIN. The user's password is securely encoded before saving.
         *
         * @return The created admin user.
         */
        User createAdminUser() {
            return createUser(ADMIN_USERNAME, Role.ADMIN);
        }

    /**
         * Creates and returns a simple user with a predefined username and role.
         * The simple user is assigned the username specified by the constant USER_USERNAME
         * and the role Role.USER. The user's password is securely encoded before saving.
         *
         * @return The created simple user.
         */
        User createSimpleUser() {
            return createUser(USER_USERNAME, Role.USER);
        }

    /**
         * Creates and returns a user with the specified username and role.
         * The user's password is securely encoded before saving.
         *
         * @param username The username for the new user.
         * @param role The role to be assigned to the new user.
         * @return The created user.
         */
        private User createUser(String username, Role role) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("securePassword"));
            user.setRole(role);
            userRepository.save(user);
            return user;
        }

    /**
         * Cleans up the test data by deleting all entries from the user, employee, and leave repositories.
         * This method ensures that the database is in a clean state after each test execution.
         */
        void cleanUp() {
            userRepository.deleteAll();
            employeeRepository.deleteAll();
            leaveRepository.deleteAll();
        }
}
