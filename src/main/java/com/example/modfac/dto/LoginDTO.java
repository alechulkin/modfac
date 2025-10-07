package com.example.modfac.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    /**
     * Data Transfer Object (DTO) for handling login information.
     * This class is used to encapsulate the username and password
     * provided by the user during the login process.
     */
    /**
         * The username provided by the user during the login process.
         * This field is mandatory and cannot be blank.
         */
        @NotBlank
        private String username;
    
    /**
         * The password provided by the user during the login process.
         * This field is mandatory and cannot be blank.
         */
        @NotBlank
        private String password;
}