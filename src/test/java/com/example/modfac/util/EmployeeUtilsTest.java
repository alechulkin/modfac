package com.example.modfac.util;

import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.model.LeaveType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeUtilsTest {
    /**
     * Unit tests for the EmployeeUtils class.
     *
     * This class contains test cases to verify the functionality of methods in the EmployeeUtils class,
     * including operations for filling job information, filling address details, and resetting leave information.
     * Each test ensures that the methods behave as expected under various scenarios.
     */

    /**
         * Tests the {@link EmployeeUtils#fillJobInfo(Employee.JobInfo, OnboardEmployeeDTO, Employee)} method.
         *
         * This test verifies that the method correctly copies fields from the given
         * {@link OnboardEmployeeDTO} and {@link Employee} (acting as the manager) to the
         * {@link Employee.JobInfo} object. It ensures that all fields, including email,
         * hire date, job ID, salary, and manager, are properly transferred.
         */

    /**
         * Tests the {@link EmployeeUtils#fillAddress(Employee.Address, OnboardEmployeeDTO)} method.
         *
         * This test verifies that the method correctly copies fields from the given
         * {@link OnboardEmployeeDTO} to the {@link Employee.Address} object. It ensures
         * that all address-related fields, including street, city, state, and zip code,
         * are properly transferred.
         */

    /**
         * Tests the {@link EmployeeUtils#resetLeaveInfo(Employee)} method.
         *
         * This test ensures that the resetLeaveInfo method correctly resets all leave types
         * in the employee's leave information to zero. It verifies that the resulting leave
         * information contains all leave types with their values set to zero, ensuring no
         * data is left uninitialized or incorrect.
         */
}

