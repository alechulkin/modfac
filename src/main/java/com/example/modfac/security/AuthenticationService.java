package com.example.modfac.security;

import com.example.modfac.dto.LoginDTO;
import com.example.modfac.dto.RegisterUserDTO;
import com.example.modfac.exception.ResourceNotFoundException;
import com.example.modfac.exception.UsernameAlreadyExistsException;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.UserRepository;
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
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Create a new user
     */
    @Transactional
    public User createUser(RegisterUserDTO registerDto) {
        String username = registerDto.getUsername();
        log.info("Creating user with username: {}", username);

        return registerUser(registerDto, Role.USER);
    }

    /**
     * Create an admin user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public User createAdmin(RegisterUserDTO registerDto) {
        log.info("Creating admin user with username: {}", registerDto.getUsername());

        return registerUser(registerDto, Role.ADMIN);
    }

    private User registerUser(RegisterUserDTO registerDto, Role role) {
        String username = registerDto.getUsername();
        Optional<User> found = userRepository.findByUsername(username);

        if (found.isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Authenticate a user and return JWT token
     */
    public String login(LoginDTO loginDto) {
        log.info("Authenticating user: {}", loginDto.getUsername());

        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return jwtTokenProvider.createToken(user.getUsername(), user.getRole().toString());
    }

    /**
     * Get current user information
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
