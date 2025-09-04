package com.example.modfac.controller;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.model.Leave;
import com.example.modfac.service.LeaveService;
import com.example.modfac.service.MainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@Slf4j
@RequiredArgsConstructor
public class LeaveController {
    private final MainService mainService;

    @PostMapping
    public ResponseEntity<Leave> requestLeave(@Valid @RequestBody CaptureLeaveDTO dto) {

        log.info("Processing leave request for employee ID: {}, type: {}", dto.getEmployeeId(), dto.getLeaveType());

        mainService.capture(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}