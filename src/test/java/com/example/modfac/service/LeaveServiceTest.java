package com.example.modfac.service;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.exception.InsufficientLeaveBalanceException;
import com.example.modfac.model.*;
import com.example.modfac.repository.LeaveRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private CaptureLeaveDTO captureLeaveDto;
    private Employee employee;
    private Leave leave;

    @BeforeEach
    void setUp() {
        // Setup test data
        captureLeaveDto = new CaptureLeaveDTO();
        captureLeaveDto.setLeaveType(LeaveType.PTO);
        captureLeaveDto.setStartDate(LocalDate.now());
        captureLeaveDto.setEndDate(captureLeaveDto.getStartDate().plusDays(5));
        captureLeaveDto.setStatus(Status.PENDING);

        employee = new Employee();
        employee.setId(new ObjectId());

        Employee.JobInfo jobInfo = new Employee.JobInfo();
        Employee manager = new Employee();
        manager.setId(new ObjectId());
        jobInfo.setManager(manager);
        employee.setJobInfo(jobInfo);

        EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
        leaveInfo.put(LeaveType.PTO, 10);
        employee.setLeaveInfo(leaveInfo);

        leave = new Leave();
        leave.setId(new ObjectId());
        leave.setEmployee(employee);
        leave.setLeaveType(captureLeaveDto.getLeaveType());
        leave.setStartDate(captureLeaveDto.getStartDate());
        leave.setEndDate(captureLeaveDto.getEndDate());
        leave.setStatus(captureLeaveDto.getStatus());
        leave.setApprovedBy(manager);

    }

    // ========== CAPTURE LEAVE TESTS ==========

    @Test
    void capture_ShouldCreateLeaveWhenSufficientBalance() {
        // Arrange
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);

        // Act
        Map.Entry<Leave, Integer> result = leaveService.capture(captureLeaveDto, employee);

        // Assert
        assertNotNull(result);

        assertEquals(LeaveType.PTO, result.getKey().getLeaveType());
        assertEquals(4, result.getValue()); // 10 initial - (5+1) days leave

        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void capture_ShouldThrowWhenInsufficientBalance() {
        // Arrange
        captureLeaveDto.setEndDate(LocalDate.now().plusDays(15));

        // Act & Assert
        assertThrows(InsufficientLeaveBalanceException.class,
                () -> leaveService.capture(captureLeaveDto, employee));
    }

    @Test
    void capture_ShouldThrowWhenZeroBalance() {
        // Arrange
        captureLeaveDto.setLeaveType(LeaveType.SICK);

        // Act & Assert
        assertThrows(InsufficientLeaveBalanceException.class,
                () -> leaveService.capture(captureLeaveDto, employee));
    }

    @Test
    void capture_ShouldCalculateCorrectLeaveDays() {
        // Arrange
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);

        // 7 days leave (Mon-Sun)
        LocalDate start = LocalDate.of(2023, 1, 2); // Monday
        LocalDate end = LocalDate.of(2023, 1, 8);   // Sunday
        captureLeaveDto.setStartDate(start);
        captureLeaveDto.setEndDate(end);

        // Act
        Map.Entry<Leave, Integer> result = leaveService.capture(captureLeaveDto, employee);

        // Assert
        assertEquals(3, result.getValue()); // 10 - (8-2+1) = 3 work days (Mon-Fri)
    }

    @Test
    void capture_ShouldHandleSingleDayLeave() {
        // Arrange
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);

        LocalDate singleDay = LocalDate.now();
        captureLeaveDto.setStartDate(singleDay);
        captureLeaveDto.setEndDate(singleDay);

        // Act
        Map.Entry<Leave, Integer> result = leaveService.capture(captureLeaveDto, employee);

        // Assert
        assertEquals(9, result.getValue()); // 10-1=9 day leave
    }

    // ========== GENERATE LEAVE TESTS ==========

    @Test
    void generateLeave_ShouldCreateLeaveWithRandomValues() {
        // Arrange
        Employee manager = new Employee();
        manager.setId(new ObjectId());

        when(leaveRepository.save(any(Leave.class))).thenReturn(new Leave());

        // Act
        leaveService.generateLeave(employee, manager);

        // Assert
        verify(leaveRepository, times(1)).save(any(Leave.class));
    }

    @Test
    void generateLeave_ShouldSetApproverOnlyForApprovedStatus() {
        // Arrange
        Employee manager = new Employee();
        manager.setId(new ObjectId());

        when(leaveRepository.save(any(Leave.class))).thenAnswer(invocation -> {
            Leave l = invocation.getArgument(0);
            if (l.getStatus() == Status.APPROVED) {
                assertNotNull(l.getApprovedBy());
            } else {
                assertNull(l.getApprovedBy());
            }
            return l;
        });

        // Act - run multiple times to cover different random statuses
        for (int i = 0; i < 10; i++) {
            leaveService.generateLeave(employee, manager);
        }

        // Assert is handled in the mock answer
        verify(leaveRepository, atLeastOnce()).save(any(Leave.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void capture_ShouldHandleNullEmployee() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> leaveService.capture(captureLeaveDto, null));
    }

    @Test
    void capture_ShouldHandleNullLeaveInfo() {
        // Arrange
        employee.setLeaveInfo(null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> leaveService.capture(captureLeaveDto, employee));
    }
}