package com.example.modfac.controller;

import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EmployeeController.class);
    private final DataService dataService;
    
    @PostMapping
    public ResponseEntity<Employee> onboardEmployee(
            @Valid @RequestBody OnboardEmployeeDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Employee employee = onboard(dto, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    //used for testing only
    @PostMapping(path = "many")
    public ResponseEntity<List<Employee>> onboardEmployees(
            @Valid @RequestBody List<OnboardEmployeeDTO> dtos,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Employee> result = new ArrayList<>();
        for (OnboardEmployeeDTO dto: dtos) {
            result.add(onboard(dto, userDetails));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    private Employee onboard(OnboardEmployeeDTO dto, UserDetails userDetails) {
        dto.setCreatedBy(userDetails.getUsername());
        LOG.info("Received request to onboard employee: {} {}",
                dto.getFirstName(), dto.getLastName());
    
        return dataService.onboard(dto);
    }
}