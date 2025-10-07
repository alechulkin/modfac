package com.example.modfac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Data
public class SearchEmployeeByNameDTO {
    /**
     * Data Transfer Object (DTO) for searching employees by name.
     * <p>
     * This class is used to encapsulate the search criteria, including the name of the employee
     * to search for, as well as pagination parameters such as page number and page size.
     * </p>
     */
    @NotBlank(message = "Search name is required")
    @Size(min = 3, message = "Search term must be at least 2 characters")
    private String name;

    private int page = 0;

    private int size = 10;

}

