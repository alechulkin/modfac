package com.example.modfac.util;

import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.model.Employee;
import com.example.modfac.model.LeaveType;

import java.util.EnumMap;

public final class EmployeeUtils {
    private EmployeeUtils() {}

    public static Employee.JobInfo fillJobInfo(Employee.JobInfo jobInfo, OnboardEmployeeDTO dto, Employee manager) {
        jobInfo.setEmail(dto.getEmail());
        jobInfo.setHireDate(dto.getHireDate());
        jobInfo.setJobId(dto.getJobId());
        jobInfo.setSalary(dto.getSalary());
        jobInfo.setManager(manager);

        return jobInfo;
    }

    public static Employee.Address fillAddress(Employee.Address address, OnboardEmployeeDTO dto) {
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setCountry(dto.getState());
        address.setZipCode(dto.getZipCode());

        return address;
    }

    public static Employee resetLeaveInfo(Employee employee) {
        EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
        for (LeaveType leaveType : LeaveType.values()) {
            leaveInfo.put(leaveType, 0);
        }
        employee.setLeaveInfo(leaveInfo);
        return employee;
    }
}
