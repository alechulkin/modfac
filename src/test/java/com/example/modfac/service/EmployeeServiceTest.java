package com.example.modfac.service;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.dto.SearchEmployeeByNameDTO;
import com.example.modfac.exception.LeaveNotApprovedByManagerException;
import com.example.modfac.exception.ResourceNotFoundException;
import com.example.modfac.model.*;
import com.example.modfac.repository.EmployeeRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    /**
     * Unit tests for the EmployeeService class.
     *
     * This class uses JUnit and Mockito to test the functionality of the EmployeeService.
     * It includes tests for onboarding employees, searching employees by name,
     * generating employees, finding employees by ID, verifying user and manager relationships,
     * and updating leave information.
     */

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private OnboardEmployeeDTO onboardDto;
    private Employee existingEmployee;
    private Employee newEmployee;
    private CaptureLeaveDTO captureLeaveDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        onboardDto = new OnboardEmployeeDTO();
        onboardDto.setFirstName("John");
        onboardDto.setLastName("Doe");
        onboardDto.setStreet("123 Main St");
        onboardDto.setCity("New York");
        onboardDto.setState("NY");
        onboardDto.setZipCode("10001");
        onboardDto.setPhoneNumber("+1234567890");
        onboardDto.setEmail("john.doe@example.com");
        onboardDto.setHireDate(LocalDate.now());
        onboardDto.setJobId("DEV001");
        onboardDto.setSalary(80000);
        onboardDto.setManagerId(new ObjectId().toString());

        existingEmployee = new Employee();
        existingEmployee.setId(new ObjectId());
        existingEmployee.setPhoneNumber(onboardDto.getPhoneNumber());

        newEmployee = new Employee();
        newEmployee.setId(new ObjectId());
        newEmployee.setFirstName(onboardDto.getFirstName());
        newEmployee.setLastName(onboardDto.getLastName());
        newEmployee.setPhoneNumber(onboardDto.getPhoneNumber());

        captureLeaveDto = new CaptureLeaveDTO();
        captureLeaveDto.setEmployeeId(new ObjectId().toString());
        captureLeaveDto.setApprovedById(new ObjectId().toString());
    }

    // ========== ONBOARD TESTS ==========

    /**
         * Tests the onboarding process for an existing employee.
         *
         * This test verifies that when an employee with the same phone number already exists,
         * the existing employee's information is updated instead of creating a new employee.
         * It ensures that the repository's save method is called with the existing employee.
         */
        @Test
        void onboard_ShouldUpdateExistingEmployee() {
            // Arrange
            when(employeeRepository.findEmployeeByPhoneNumber(anyString()))
                    .thenReturn(Optional.of(existingEmployee));
            when(employeeRepository.findById(any(ObjectId.class)))
                    .thenReturn(Optional.of(new Employee())); // manager
            when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);
    
            // Act
            Employee result = employeeService.onboard(onboardDto);
    
            // Assert
            assertNotNull(result);
            assertEquals(existingEmployee.getId(), result.getId());
            verify(employeeRepository, times(1)).save(existingEmployee);
        }

    /**
         * Tests the onboarding process for a new employee.
         *
         * This test verifies that when no employee with the same phone number exists,
         * a new employee is created and saved in the repository.
         * It ensures that the repository's save method is called with the new employee.
         */
        @Test
        void onboard_ShouldCreateNewEmployee() {
            // Arrange
            when(employeeRepository.findEmployeeByPhoneNumber(anyString()))
                    .thenReturn(Optional.empty());
            when(employeeRepository.findById(any(ObjectId.class)))
                    .thenReturn(Optional.of(new Employee())); // manager
            when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);
    
            // Act
            Employee result = employeeService.onboard(onboardDto);
    
            // Assert
            assertNotNull(result);
            assertEquals(newEmployee.getId(), result.getId());
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

    // ========== SEARCH TESTS ==========

    /**
         * Tests the search functionality for employees by name.
         *
         * This test verifies that the search method returns a list of employees
         * whose names match the search criteria. It ensures that the repository's
         * searchByName method is called with the correct parameters and that the
         * returned list contains the expected employees.
         */
        @Test
        void search_ShouldReturnEmployees() {
            // Arrange
            SearchEmployeeByNameDTO searchDto = new SearchEmployeeByNameDTO();
            searchDto.setName("John");
    
            List<Employee> expectedEmployees = List.of(newEmployee);
            when(employeeRepository.searchByName(anyString(), any(Pageable.class))).thenReturn(expectedEmployees);
    
            // Act
            List<Employee> result = employeeService.search(searchDto);
    
            // Assert
            assertEquals(1, result.size());
            verify(employeeRepository, times(1)).searchByName("John",
                    PageRequest.of(0, 10));
        }

    // ========== GENERATE EMPLOYEES TESTS ==========


    /**
         * Tests the generateEmployees method to ensure it creates the specified number of employees.
         *
         * This test verifies that the repository's save method is called the correct number of times
         * when generating employees. It ensures that the generateEmployees method behaves as expected
         * when provided with a valid number of employees to create.
         */
        @Test
        void generateEmployees_ShouldCreateEmployees() {
            // Arrange
            when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);
            int numEmployees = 3;
    
            // Act
            employeeService.generateEmployees(numEmployees);
    
            // Assert
            verify(employeeRepository, times(numEmployees)).save(any(Employee.class));
        }

    // ========== FIND BY ID TESTS ==========

    /**
         * Tests the findById method to ensure it retrieves an employee by their ID.
         *
         * This test verifies that when a valid employee ID is provided, the method
         * returns the corresponding employee. It ensures that the repository's
         * findById method is called with the correct ID and that the returned
         * employee matches the expected result.
         */
        @Test
        void findById_ShouldReturnEmployee() {
            // Arrange
            ObjectId employeeId = new ObjectId();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(newEmployee));
    
            // Act
            Employee result = employeeService.findById(employeeId);
    
            // Assert
            assertNotNull(result);
            assertEquals(newEmployee.getId(), result.getId());
        }

    /**
         * Tests the findById method to ensure it throws an exception when the employee is not found.
         *
         * This test verifies that when an invalid employee ID is provided, the method
         * throws a ResourceNotFoundException. It ensures that the repository's findById
         * method is called with the correct ID and that the exception is properly thrown.
         */
        @Test
        void findById_ShouldThrowWhenNotFound() {
            // Arrange
            ObjectId employeeId = new ObjectId();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
    
            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(employeeId));
        }

    // ========== VERIFY USER AND MANAGER TESTS ==========

    /**
         * Tests the verifyUserAndItsManagerAndApprover method to ensure it throws an exception
         * when the manager of the employee does not match the approver.
         *
         * This test verifies that when the manager assigned to the employee's job information
         * is different from the approver provided in the CaptureLeaveDTO, the method throws
         * a LeaveNotApprovedByManagerException. It ensures that the validation logic for
         * manager-approver mismatch is functioning correctly.
         */
        @Test
        void verifyUserAndItsManagerAndApprover_ShouldThrowWhenManagerMismatch() {
            // Arrange
            Employee employee = new Employee();
            Employee.JobInfo jobInfo = new Employee.JobInfo();
            Employee manager = new Employee();
            manager.setId(new ObjectId());
            jobInfo.setManager(manager);
            employee.setJobInfo(jobInfo);
    
            when(employeeRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(employee));
    
            // Act & Assert
            assertThrows(LeaveNotApprovedByManagerException.class,
                    () -> employeeService.verifyUserAndItsManagerAndApprover(captureLeaveDto));
        }

    /**
         * Tests the verifyUserAndItsManagerAndApprover method to ensure it returns the employee
         * when the provided approver ID matches the manager ID in the employee's job information.
         *
         * This test verifies that the method correctly identifies a valid manager-approver relationship
         * and returns the employee without throwing any exceptions. It ensures that the validation logic
         * for manager-approver matching is functioning as expected.
         */
        @Test
        void verifyUserAndItsManagerAndApprover_ShouldReturnEmployeeWhenValidJobManagerId() {
            // Arrange
            Employee employee = new Employee();
            Employee.JobInfo jobInfo = new Employee.JobInfo();
            employee.setJobInfo(jobInfo);
            ObjectId managerId = new ObjectId(captureLeaveDto.getApprovedById());
            Employee manager = new Employee();
            manager.setId(managerId);
    
            jobInfo.setManager(manager);
    
            when(employeeRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(employee));
    
            // Act
            Employee result = employeeService.verifyUserAndItsManagerAndApprover(captureLeaveDto);
    
            // Assert
            assertNotNull(result);
        }

    /**
         * Tests the verifyUserAndItsManagerAndApprover method to ensure it returns the employee
         * when the provided approver ID matches the employee's own ID.
         *
         * This test verifies that the method correctly identifies a valid self-manager relationship
         * and returns the employee without throwing any exceptions. It ensures that the validation logic
         * for self-manager matching is functioning as expected.
         */
        @Test
        void verifyUserAndItsManagerAndApprover_ShouldReturnEmployeeWhenValidSelfManagerId() {
            // Arrange
            Employee employee = new Employee();
            Employee.JobInfo jobInfo = new Employee.JobInfo();
            employee.setJobInfo(jobInfo);
            ObjectId managerId = new ObjectId(captureLeaveDto.getApprovedById());
            employee.setId(managerId);
    
            when(employeeRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(employee));
    
            // Act
            Employee result = employeeService.verifyUserAndItsManagerAndApprover(captureLeaveDto);
    
            // Assert
            assertNotNull(result);
        }

    // ========== UPDATE LEAVE INFO TESTS ==========

    /**
         * Tests the updateLeaveInfo method to ensure it updates the leave information of an employee.
         *
         * This test verifies that when a leave entry is provided, the method updates the employee's
         * leave information correctly. It ensures that the repository's save method is called with
         * the updated employee and that the leave information reflects the changes.
         */
        @Test
        void updateLeaveInfo_ShouldUpdateEmployeeLeaveInfo() {
            // Arrange
            Employee employee = new Employee();
            EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
            leaveInfo.put(LeaveType.PTO, 10);
            employee.setLeaveInfo(leaveInfo);
    
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
    
            // Act
            Leave leave = new Leave();
            leave.setLeaveType(LeaveType.PTO);
            Map.Entry<Leave, Integer> leaveEntry = new AbstractMap.SimpleEntry<>(leave, 5);
            employeeService.updateLeaveInfo(employee, leaveEntry);
    
            // Assert
            assertEquals(5, employee.getLeaveInfo().get(LeaveType.PTO));
            verify(employeeRepository, times(1)).save(employee);
        }
}