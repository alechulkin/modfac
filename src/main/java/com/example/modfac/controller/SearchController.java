package com.example.modfac.controller;

import com.example.modfac.dto.SearchEmployeeByNameDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Slf4j
@Validated
@RequiredArgsConstructor
public class SearchController {
    private final EmployeeService employeeService;

    @PostMapping("/employees")
    public ResponseEntity<List<Employee>> searchEmployees(@Valid @RequestBody SearchEmployeeByNameDTO dto) {
        log.info("Searching for employees with parameters: {}", dto);
        List<Employee> employees = employeeService.search(dto);
        return ResponseEntity.ok(employees);
    }
}