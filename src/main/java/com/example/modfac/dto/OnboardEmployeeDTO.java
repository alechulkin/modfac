package com.example.modfac.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class OnboardEmployeeDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Street is required")
    @Size(max = 100, message = "Street must be less than 100 characters")
    private String street;
    
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must be less than 50 characters")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must be less than 50 characters")
    private String state;
    
    @NotBlank(message = "Zip code is required")
    @Size(max = 10, message = "Zip code must be less than 10 characters")
    private String zipCode;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\-\\s]+$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @NotBlank(message = "Job ID is required")
    @Size(max = 10, message = "Job ID must be less than 10 characters")
    private String jobId;
    
    @NotNull(message = "Salary is required")
    @Min(value = 0, message = "Salary cannot be negative")
    private Integer salary;
    
    private String managerId;
    
    private String createdBy;
}
