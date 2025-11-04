package com.example.modfac.service;

import com.example.modfac.exception.InsufficientLeaveBalanceException;
import com.example.modfac.model.Employee;
import com.example.modfac.model.Leave;
import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Status;

import static com.example.modfac.util.LeaveUtils.*;
import com.example.modfac.repository.LeaveRepository;
import com.example.modfac.dto.CaptureLeaveDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    
    /**
     * Process the leave capture request with updated document structure
     */
        public Map.Entry<Leave, Integer> capture(CaptureLeaveDTO leaveDTO, Employee employee) {
            log.debug("capture method invoked");
    
            LocalDate startDate = leaveDTO.getStartDate();
            LocalDate endDate = leaveDTO.getEndDate();
            int leaveDays = getLeaveDays(startDate, endDate);
    
            LeaveType leaveType = leaveDTO.getLeaveType();
            Integer balance = employee.getLeaveInfo().get(leaveType);
            if (balance == null) {
                balance = 0;
            }
            if (balance < leaveDays) {
                log.warn("Requested {} days but only {} available", leaveDays, balance);
                throw new InsufficientLeaveBalanceException(
                    "Insufficient leave balance. Available: " + balance + 
                    ", Requested: " + leaveDays);
            }
    
            Employee manager = employee.getJobInfo().getManager();
            Leave leave = new Leave();
            leave.setEmployee(employee);
            leave.setLeaveType(leaveType);
            leave.setStartDate(startDate);
            leave.setEndDate(endDate);
            leave.setStatus(leaveDTO.getStatus());
            leave.setApprovedBy(manager);
            leave = leaveRepository.save(leave);
            log.info("Leave request processed successfully with ID: {}", leave.getId());
    
            log.debug("capture method finished");
            return new EnumMap.SimpleEntry<>(leave, balance - leaveDays);
        }

        @Transactional
        public void generateLeave(Employee employee, Employee manager) {
            log.debug("generateLeave method invoked");
    
            Leave leave = new Leave();
    
            // Random status
            Status status = getRandomStatus();
            leave.setStatus(status);
    
            // Random leave type
            LeaveType leaveType = getRandomLeaveType();
            leave.setLeaveType(leaveType);
    
            // Random start date
            LocalDate startDate = getRandomDate(LEAVE_PERIOD_STARTING_DATE);
            LocalDate endDate = getRandomDate(startDate);
            leave.setStartDate(startDate);
            leave.setEndDate(endDate);
            leave.setEmployee(employee);
    
            // Random approved by (only if status is APPROVED)
            if (status == Status.APPROVED) {
                leave.setApprovedBy(manager);
            }
            leaveRepository.save(leave);
    
            log.debug("generateLeave method finished");
        }

}