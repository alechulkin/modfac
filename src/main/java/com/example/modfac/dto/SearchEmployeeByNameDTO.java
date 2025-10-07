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
    /**
     * The name of the employee to search for.
     * <p>
     * This field is mandatory and must meet the following validation constraints:
     * <ul>
     *   <li>Cannot be blank.</li>
     *   <li>Must be at least 3 characters long.</li>
     * </ul>
     * </p>
     */

    /**
     * The page number for pagination.
     * <p>
     * This field determines the current page of the search results to retrieve.
     * The default value is 0, which corresponds to the first page.
     * </p>
     */

    /**
     * The number of records per page for pagination.
     * <p>
     * This field determines how many records are displayed on each page of the search results.
     * The default value is 10.
     * </p>
     */

}

