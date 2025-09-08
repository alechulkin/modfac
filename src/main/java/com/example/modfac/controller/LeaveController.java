package com.example.modfac.controller;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.model.Leave;
import com.example.modfac.service.DataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@Slf4j
@RequiredArgsConstructor
public class LeaveController {
    private final DataService dataService;

    @PostMapping
    public ResponseEntity<Leave> requestLeave(@Valid @RequestBody CaptureLeaveDTO dto) {

        log.info("Processing leave request for employee ID: {}, type: {}", dto.getEmployeeId(), dto.getLeaveType());

        dataService.capture(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}