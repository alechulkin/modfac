package com.example.modfac.service;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.exception.LeaveNotApprovedByManagerException;
import com.example.modfac.model.Employee;
import com.example.modfac.model.Leave;
import com.example.modfac.model.LeaveType;
import com.example.modfac.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MainService {
    public static final int EMPLOYEES_NUMBER = 20;

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final UserService userService;

    @Transactional
    public Employee onboard(OnboardEmployeeDTO dto) {
        log.info("Processing onboarding for employee: {} {}",
                dto.getFirstName(), dto.getLastName());

        // Check if the user is an admin
        userService.verifyAdminUser(dto.getCreatedBy());
        return employeeService.onboard(dto);
    }

    @Transactional
    public Leave capture(CaptureLeaveDTO dto) {
        log.info("Processing leave request for employee ID: {}, type: {}", dto.getEmployeeId(), dto.getLeaveType());

        Employee employee = employeeService.verifyUserAndItsManagerAndApprover(dto);
        Map.Entry<Leave, Integer> captureResult = leaveService.capture(dto, employee);
        employeeService.updateLeaveInfo(employee, captureResult);
        return captureResult.getKey();
    }

    @Transactional
    public void generateData() {
        Map<Employee, Employee> employeeEmployeeMap = employeeService.generateEmployees(EMPLOYEES_NUMBER);
        for (Map.Entry<Employee, Employee> entry : employeeEmployeeMap.entrySet()) {
            Employee employee = entry.getKey();
            Employee manager = entry.getValue();
            for (int j = 0; j < EMPLOYEES_NUMBER; j++) {
                leaveService.generateLeave(employee, manager);
            }
        }
        userService.generateUsers();
    }
}