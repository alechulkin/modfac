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
    /**
     * Data Transfer Object (DTO) for capturing leave details.
     * <p>
     * This class is used to encapsulate the data required for leave management,
     * including employee ID, leave type, start and end dates, status, approver ID,
     * and an optional reason for the leave. It includes validation annotations to
     * ensure the integrity of the data.
     */
    /**
     * The unique identifier for an employee.
     * <p>
     * This field is mandatory and must not be blank. It is used to associate leave
     * details with a specific employee in the system.
     */
    private String employeeId;
    
    /**
     * The type of leave being requested.
     * <p>
     * This field is mandatory and must not be null. It is used to specify the category
     * of leave, such as vacation, sick leave, or maternity leave.
     */
    private LeaveType leaveType;
    
    /**
     * The start date of the leave period.
     * <p>
     * This field is mandatory and must not be null. It must represent a date that is
     * either today or in the future, as specified by the validation constraints.
     */
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    
    /**
     * The end date of the leave period.
     * <p>
     * This field is mandatory and must not be null. It must represent a date that is
     * in the future, as specified by the validation constraints.
     */
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    /**
     * The current status of the leave request.
     * <p>
     * This field indicates the state of the leave request, such as pending, approved,
     * or rejected. It is used to track the progress and outcome of the leave process.
     */
    private Status status;
    
    /**
     * The unique identifier of the approver for the leave request.
     * <p>
     * This field is mandatory and must not be blank. It is used to record the ID of the person
     * who approves the leave request, ensuring accountability and traceability in the approval process.
     */
    @NotBlank(message = "Approver ID is required")
    private String approvedById;
    
    /**
     * The reason for the leave request.
     * <p>
     * This field is optional and can contain up to 500 characters. It is used to provide
     * additional context or justification for the leave request, helping approvers make
     * informed decisions.
     */
    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason;
}
