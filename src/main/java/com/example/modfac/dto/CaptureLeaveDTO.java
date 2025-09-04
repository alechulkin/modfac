package com.example.modfac.dto;

import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Status;

import com.example.modfac.validation.ValidLeaveDates;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@ValidLeaveDates
@Data
public class CaptureLeaveDTO {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;
    
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    private Status status;
    
    @NotBlank(message = "Approver ID is required")
    private String approvedById;
    
    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason;
}
