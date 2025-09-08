package com.example.modfac.controller;

import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {
    private final DataService dataService;

    @PostMapping
    public ResponseEntity<Employee> onboardEmployee(
            @Valid @RequestBody OnboardEmployeeDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        dto.setCreatedBy(userDetails.getUsername());
        log.info("Received request to onboard employee: {} {}",
                dto.getFirstName(), dto.getLastName());

        Employee employee = dataService.onboard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }
}
