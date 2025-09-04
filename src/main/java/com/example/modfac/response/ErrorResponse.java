package com.example.modfac.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private Map<String, String> details;
}
