package com.example.modfac.controller;

import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.repository.EmployeeRepository;
import com.example.modfac.service.EmployeeService;
import com.example.modfac.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {
    private final MainService mainService;

    @PostMapping
    public ResponseEntity<Employee> onboardEmployee(
            @Valid @RequestBody OnboardEmployeeDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        dto.setCreatedBy(userDetails.getUsername());
        log.info("Received request to onboard employee: {} {}",
                dto.getFirstName(), dto.getLastName());

        Employee employee = mainService.onboard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }
}
