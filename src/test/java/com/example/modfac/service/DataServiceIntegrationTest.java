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
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.config.name=application-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DataServiceIntegrationTest {
    /**
     * Integration tests for the DataService class.
     *
     * This class verifies the functionality of the DataService methods by interacting
     * with the database and other dependent components. It ensures that the service
     * behaves as expected under various scenarios, including onboarding employees
     * and capturing leave requests.
     *
     * The tests use a test-specific application context and reset the database state
     * after each test to ensure isolation and repeatability.
     */

    @Autowired
    private DataService dataService;
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

    /**
         * Initializes test data before each test case.
         *
         * This method sets up the necessary entities, such as admin users, managers,
         * employees, and DTOs, to ensure that each test starts with a consistent and
         * isolated state. It also populates the database with these entities to
         * facilitate integration testing.
         */
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

    /**
         * Cleans up test data after each test case.
         *
         * This method ensures that the database is reset to a clean state by
         * deleting all records from the leave, employee, and user repositories.
         * This guarantees test isolation and prevents data leakage between tests.
         */
        @AfterEach
        void tearDown() {
            leaveRepository.deleteAll();
            employeeRepository.deleteAll();
            userRepository.deleteAll();
        }

    /**
         * Tests the onboarding functionality for admin users.
         *
         * This test verifies that an admin user can successfully onboard a new employee.
         * It ensures that the created employee has the expected attributes, including
         * first name, last name, and job information, and that the manager is correctly assigned.
         */
        @Test
        void onboard_ShouldCreateEmployeeWhenAdminUser() {
            // Act
            Employee result = dataService.onboard(onboardDto);
    
            // Assert
            assertNotNull(result.getId());
            assertEquals(onboardDto.getFirstName(), result.getFirstName());
            assertEquals(onboardDto.getLastName(), result.getLastName());
            assertNotNull(result.getJobInfo());
            assertEquals(manager.getId(), result.getJobInfo().getManager().getId());
        }

    /**
         * Tests the onboarding functionality for non-admin users.
         *
         * This test verifies that a non-admin user attempting to onboard a new employee
         * results in an UnauthorizedException being thrown. It ensures that only admin
         * users have the necessary permissions to perform the onboarding operation.
         */
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
            assertThrows(UnauthorizedException.class, () -> dataService.onboard(onboardDto));
        }

    /**
         * Tests the leave capture functionality and leave balance update.
         *
         * This test verifies that a leave request can be successfully captured
         * and that the employee's leave balance is updated accordingly. It ensures
         * that the created leave has the expected attributes, including the employee,
         * approver, and leave type. Additionally, it checks that the leave balance
         * is decremented by the correct amount.
         */
        @Test
        void capture_ShouldCreateLeaveAndUpdateBalance() {
            // Act
            Leave result = dataService.capture(captureLeaveDto);
    
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

    /**
         * Tests the leave capture functionality when the approving manager does not match.
         *
         * This test verifies that attempting to capture a leave request with an approver
         * who is not the employee's manager results in a LeaveNotApprovedByManagerException
         * being thrown. It ensures that leave requests are only approved by the correct manager.
         */
        @Test
        void capture_ShouldThrowWhenManagerMismatch() {
            // Arrange
            Employee otherManager = new Employee();
            otherManager.setFirstName("Other");
            otherManager.setLastName("Manager");
            otherManager = employeeRepository.save(otherManager);
    
            captureLeaveDto.setApprovedById(otherManager.getId().toString());
    
            // Act & Assert
            assertThrows(LeaveNotApprovedByManagerException.class, () -> dataService.capture(captureLeaveDto));
        }

    /**
         * Tests the rollback functionality when leave update fails due to insufficient balance.
         *
         * This test verifies that when an employee's leave balance is insufficient to
         * accommodate a leave request, the operation is rolled back. It ensures that
         * no leave is created and the leave balance remains unchanged in such scenarios.
         */
        @Test
        void capture_ShouldRollbackWhenLeaveUpdateFails() {
            // Arrange - make employee leave balance insufficient
            employee.getLeaveInfo().put(LeaveType.PTO, 1);
            employeeRepository.save(employee);
    
            // Act & Assert
            assertThrows(InsufficientLeaveBalanceException.class, () -> dataService.capture(captureLeaveDto));
    
            // Verify no leave was created
            List<Leave> leaves = leaveRepository.findAll();
            assertEquals(0, leaves.size());
    
            // Verify leave balance wasn't changed
            Employee unchangedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
            assertEquals(1, unchangedEmployee.getLeaveInfo().get(LeaveType.PTO));
        }
}