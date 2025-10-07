package com.example.modfac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDTO {
    /**
     * Data Transfer Object (DTO) for registering a new user.
     * <p>
     * This class is used to encapsulate the data required for user registration,
     * including username and password. It includes validation constraints to ensure
     * that the provided data meets the required criteria.
     */
    /**
         * The username of the user to be registered.
         * <p>
         * This field must not be blank and must have a length between 3 and 50 characters.
         */
        private String username;
    
    /**
         * The password of the user to be registered.
         * <p>
         * This field must not be blank and must have a length between 6 and 40 characters.
         */
        private String password;
}

