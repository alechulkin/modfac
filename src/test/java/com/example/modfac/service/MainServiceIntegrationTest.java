package com.example.modfac.service;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.exception.InsufficientLeaveBalanceException;
import com.example.modfac.exception.LeaveNotApprovedByManagerException;
import com.example.modfac.exception.UnauthorizedException;
import com.example.modfac.model.*;
import com.example.modfac.repository.EmployeeRepository;
import com.example.modfac.repository.LeaveRepository;
import com.example.modfac.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.config.name=application-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MainServiceIntegrationTest {

    @Autowired
    private MainService mainService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;
    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private Employee manager;
    private Employee employee;
    private OnboardEmployeeDTO onboardDto;
    private CaptureLeaveDTO captureLeaveDto;

    private final int existingLeaveDays = 20;
    private final int newLeaveDuration = 5;

    @BeforeEach
    void setUp() {
        // Create test admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("adminpass");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        // Create test manager
        manager = new Employee();
        manager.setFirstName("Eugene");
        manager.setLastName("Smith");
        manager.setPhoneNumber("34220251132");
        manager = employeeRepository.save(manager);

        // Create test employee
        employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        Employee.JobInfo jobInfo = new Employee.JobInfo();
        jobInfo.setManager(manager);
        employee.setJobInfo(jobInfo);
        EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
        leaveInfo.put(LeaveType.PTO, existingLeaveDays);

        employee.setLeaveInfo(leaveInfo);
        employee.setPhoneNumber("000456234");
        employee = employeeRepository.save(employee);

        // Setup DTOs
        onboardDto = new OnboardEmployeeDTO();
        onboardDto.setFirstName("Test");
        onboardDto.setLastName("Employee");
        onboardDto.setCreatedBy(adminUser.getUsername());
        onboardDto.setManagerId(manager.getId().toString());

        captureLeaveDto = new CaptureLeaveDTO();
        captureLeaveDto.setEmployeeId(employee.getId().toString());
        captureLeaveDto.setApprovedById(manager.getId().toString());
        captureLeaveDto.setLeaveType(LeaveType.PTO);
        LocalDate now = LocalDate.now();
        captureLeaveDto.setStartDate(now);
        captureLeaveDto.setEndDate(now.plusDays(newLeaveDuration)); // 5 days later
    }

    @AfterEach
    void tearDown() {
        leaveRepository.deleteAll();
        employeeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void onboard_ShouldCreateEmployeeWhenAdminUser() {
        // Act
        Employee result = mainService.onboard(onboardDto);

        // Assert
        assertNotNull(result.getId());
        assertEquals(onboardDto.getFirstName(), result.getFirstName());
        assertEquals(onboardDto.getLastName(), result.getLastName());
        assertNotNull(result.getJobInfo());
        assertEquals(manager.getId(), result.getJobInfo().getManager().getId());
    }

    @Test
    void onboard_ShouldThrowWhenNonAdminUser() {
        // Arrange
        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setPassword("password");
        regularUser.setRole(Role.USER);
        userRepository.save(regularUser);

        onboardDto.setCreatedBy(regularUser.getUsername());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> mainService.onboard(onboardDto));
    }

    @Test
    void capture_ShouldCreateLeaveAndUpdateBalance() {
        // Act
        Leave result = mainService.capture(captureLeaveDto);

        // Assert
        assertNotNull(result.getId());
        assertEquals(employee.getId(), result.getEmployee().getId());
        assertEquals(manager.getId(), result.getApprovedBy().getId());
        assertEquals(LeaveType.PTO, result.getLeaveType());

        // Verify leave balance was updated
        Employee updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertEquals(existingLeaveDays - newLeaveDuration - 1,
                updatedEmployee.getLeaveInfo().get(LeaveType.PTO)); // 20 initial - 5 days
    }

    @Test
    void capture_ShouldThrowWhenManagerMismatch() {
        // Arrange
        Employee otherManager = new Employee();
        otherManager.setFirstName("Other");
        otherManager.setLastName("Manager");
        otherManager = employeeRepository.save(otherManager);

        captureLeaveDto.setApprovedById(otherManager.getId().toString());

        // Act & Assert
        assertThrows(LeaveNotApprovedByManagerException.class, () -> mainService.capture(captureLeaveDto));
    }

    @Test
    void capture_ShouldRollbackWhenLeaveUpdateFails() {
        // Arrange - make employee leave balance insufficient
        employee.getLeaveInfo().put(LeaveType.PTO, 1);
        employeeRepository.save(employee);

        // Act & Assert
        assertThrows(InsufficientLeaveBalanceException.class, () -> mainService.capture(captureLeaveDto));

        // Verify no leave was created
        List<Leave> leaves = leaveRepository.findAll();
        assertEquals(0, leaves.size());

        // Verify leave balance wasn't changed
        Employee unchangedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertEquals(1, unchangedEmployee.getLeaveInfo().get(LeaveType.PTO));
    }
}