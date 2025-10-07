package com.example.modfac.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class OnboardEmployeeDTO {
    /**
     * Data Transfer Object (DTO) for onboarding a new employee.
     * This class is used to capture and validate the necessary details
     * required for registering a new employee in the system.
     */
    /**
     * The first name of the employee.
     * <p>
     * This field is mandatory and must be between 2 and 50 characters long.
     * It cannot be blank.
     */
    private String firstName;
    
    /**
     * The last name of the employee.
     * <p>
     * This field is mandatory and must be between 2 and 50 characters long.
     * It cannot be blank.
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    /**
     * The street address of the employee.
     * <p>
     * This field is mandatory and must be less than 100 characters long.
     * It cannot be blank.
     */
    @NotBlank(message = "Street is required")
    @Size(max = 100, message = "Street must be less than 100 characters")
    private String street;
    
    /**
     * The city of the employee's address.
     * <p>
     * This field is mandatory and must be less than 50 characters long.
     * It cannot be blank.
     */
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must be less than 50 characters")
    private String city;
    
    /**
     * The state of the employee's address.
     * <p>
     * This field is mandatory and must be less than 50 characters long.
     * It cannot be blank.
     */
    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must be less than 50 characters")
    private String state;
    
    /**
     * The zip code of the employee's address.
     * <p>
     * This field is mandatory and must be less than 10 characters long.
     * It cannot be blank.
     */
    @NotBlank(message = "Zip code is required")
    @Size(max = 10, message = "Zip code must be less than 10 characters")
    private String zipCode;
    
    /**
     * The phone number of the employee.
     * <p>
     * This field is mandatory and must be less than 20 characters long.
     * It must follow a valid phone number format, which can include optional '+'
     * at the beginning, digits, hyphens, and spaces.
     */
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\-\\s]+$", message = "Invalid phone number format")
    private String phoneNumber;
    
    /**
     * The email address of the employee.
     * <p>
     * This field is mandatory and must follow a valid email format.
     * It cannot exceed 100 characters in length.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    /**
     * The hire date of the employee.
     * <p>
     * This field is mandatory and must represent a date that is not in the future.
     * It is used to record the date when the employee was hired.
     */
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    /**
     * The job ID of the employee.
     * <p>
     * This field is mandatory and must be less than 10 characters long.
     * It cannot be blank.
     */
    @NotBlank(message = "Job ID is required")
    @Size(max = 10, message = "Job ID must be less than 10 characters")
    private String jobId;
    
    /**
     * The salary of the employee.
     * <p>
     * This field is mandatory and must be a non-negative integer.
     * It represents the employee's monthly salary in the system.
     */
    @NotNull(message = "Salary is required")
    @Min(value = 0, message = "Salary cannot be negative")
    private Integer salary;
    
    /**
     * The ID of the manager to whom the employee reports.
     * <p>
     * This field is optional and can be null if the employee does not have a manager.
     */
    private String managerId;
    
    /**
     * The username of the person who created this record.
     * <p>
     * This field is used to track the creator of the employee record in the system.
     */
    private String createdBy;
}
