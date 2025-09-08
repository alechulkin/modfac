package com.example.modfac.controller;

import com.example.modfac.dto.LoginDTO;
import com.example.modfac.dto.RegisterUserDTO;
import com.example.modfac.model.User;
import com.example.modfac.response.JwtResponse;
import com.example.modfac.response.MessageResponse;
import com.example.modfac.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/register/admin")
    public ResponseEntity<?> createAdminUser(@Valid @RequestBody RegisterUserDTO registerRequest) {
        userService.createAdmin(registerRequest);
        return ResponseEntity.ok(new MessageResponse("Admin user created successfully"));
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserDTO registerRequest) {
        userService.createUser(registerRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginRequest) {
        String jwt = userService.login(loginRequest);
        String username = loginRequest.getUsername();
        User user = userService.getCurrentUser(username);

        return ResponseEntity.ok(JwtResponse.builder().username(username)
                .role(user.getRole().toString()).token(jwt).build());
    }
}
