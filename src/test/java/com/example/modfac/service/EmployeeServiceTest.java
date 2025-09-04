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

    @Test
    void findById_ShouldThrowWhenNotFound() {
        // Arrange
        ObjectId employeeId = new ObjectId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(employeeId));
    }

    // ========== VERIFY USER AND MANAGER TESTS ==========

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