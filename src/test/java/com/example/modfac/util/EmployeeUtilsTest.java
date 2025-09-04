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

    @Test
    void fillJobInfo_ShouldCopyFieldsFromDtoAndManager() {
        // Arrange
        OnboardEmployeeDTO dto = new OnboardEmployeeDTO();
        dto.setEmail("john.doe@example.com");
        dto.setHireDate(LocalDate.now());
        dto.setJobId("DEV001");
        dto.setSalary(75000);

        Employee manager = new Employee();
        manager.setId(new ObjectId());

        Employee.JobInfo jobInfo = new Employee.JobInfo();

        // Act
        Employee.JobInfo result = EmployeeUtils.fillJobInfo(jobInfo, dto, manager);

        // Assert
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getHireDate()).isEqualTo(dto.getHireDate());
        assertThat(result.getJobId()).isEqualTo(dto.getJobId());
        assertThat(result.getSalary()).isEqualTo(dto.getSalary());
        assertThat(result.getManager()).isEqualTo(manager);
    }

    @Test
    void fillAddress_ShouldCopyFieldsFromDto() {
        // Arrange
        OnboardEmployeeDTO dto = new OnboardEmployeeDTO();
        dto.setStreet("123 Main St");
        dto.setCity("New York");
        dto.setState("NY");
        dto.setZipCode("10001");

        Employee.Address address = new Employee.Address();

        // Act
        Employee.Address result = EmployeeUtils.fillAddress(address, dto);

        // Assert
        assertThat(result.getStreet()).isEqualTo(dto.getStreet());
        assertThat(result.getCity()).isEqualTo(dto.getCity());
        assertThat(result.getCountry()).isEqualTo(dto.getState());
        assertThat(result.getZipCode()).isEqualTo(dto.getZipCode());
    }

    @Test
    void resetLeaveInfo_ShouldResetAllLeaveTypesToZero() {
        // Arrange
        Employee employee = new Employee();
        EnumMap<LeaveType, Integer> initialLeaveInfo = new EnumMap<>(LeaveType.class);
        initialLeaveInfo.put(LeaveType.PTO, 5);
        initialLeaveInfo.put(LeaveType.SICK, 3);
        employee.setLeaveInfo(initialLeaveInfo);

        // Act
        Employee result = EmployeeUtils.resetLeaveInfo(employee);

        // Assert
        EnumMap<LeaveType, Integer> resultLeaveInfo = result.getLeaveInfo();
        assertThat(resultLeaveInfo).isNotNull();
        assertThat(resultLeaveInfo.keySet()).containsExactlyInAnyOrder(LeaveType.values());
        assertThat(resultLeaveInfo.values()).allMatch(v -> v == 0);
    }
}

