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

    User createAdminUser() {
        return createUser(ADMIN_USERNAME, Role.ADMIN);
    }

    User createSimpleUser() {
        return createUser(USER_USERNAME, Role.USER);
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("securePassword"));
        user.setRole(role);
        userRepository.save(user);
        return user;
    }

    void cleanUp() {
        userRepository.deleteAll();
        employeeRepository.deleteAll();
        leaveRepository.deleteAll();
    }
}
