package com.example.modfac.service;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.model.Leave;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DataService {
    public static final int EMPLOYEES_NUMBER = 20;

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final UserService userService;

    @Transactional
    public Employee onboard(OnboardEmployeeDTO dto) {
        log.debug("Entering onboard method with DTO: {}", dto);
        log.info("Processing onboarding for employee: {} {}", dto.getFirstName(), dto.getLastName());
    
        // Check if the user is an admin
        userService.verifyAdminUser(dto.getCreatedBy());
        Employee onboardedEmployee = employeeService.onboard(dto);
    
        log.debug("Exiting onboard method with onboarded employee: {}", onboardedEmployee);
        return onboardedEmployee;
    }

    @Transactional
    public Leave capture(CaptureLeaveDTO dto) {
        log.debug("Entering capture method with DTO: {}", dto);
        log.info("Processing leave request for employee ID: {}, type: {}", dto.getEmployeeId(), dto.getLeaveType());
    
        Employee employee = employeeService.verifyUserAndItsManagerAndApprover(dto);
        Map.Entry<Leave, Integer> captureResult = leaveService.capture(dto, employee);
        employeeService.updateLeaveInfo(employee, captureResult);
    
        log.debug("Exiting capture method with captured leave: {}", captureResult.getKey());
        return captureResult.getKey();
    }

    @Transactional
    public void generateData() {
        log.debug("Entering generateData method");
    
        Map<Employee, Employee> employeeEmployeeMap = employeeService.generateEmployees(EMPLOYEES_NUMBER);
        for (Map.Entry<Employee, Employee> entry : employeeEmployeeMap.entrySet()) {
            Employee employee = entry.getKey();
            Employee manager = entry.getValue();
            for (int j = 0; j < EMPLOYEES_NUMBER; j++) {
                leaveService.generateLeave(employee, manager);
            }
        }
        userService.generateUsers();
    
        log.debug("Exiting generateData method");
    }
}