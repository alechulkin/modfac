package com.example.modfac.service;

import com.example.modfac.dto.CaptureLeaveDTO;
import com.example.modfac.dto.OnboardEmployeeDTO;
import com.example.modfac.dto.SearchEmployeeByNameDTO;
import com.example.modfac.exception.LeaveNotApprovedByManagerException;
import com.example.modfac.exception.ResourceNotFoundException;
import com.example.modfac.model.*;
import com.example.modfac.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.modfac.util.EmployeeUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public static final List<String> FIRST_NAMES = List.of("John", "Emily", "Michael", "Sarah", "William", "Olivia", "James", "Ava", "Robert", "Isabella");
    private static final List<String> LAST_NAMES = List.of("Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor");
    private static final List<String> STREET_NAMES = List.of("Main St", "Park Ave", "Elm St", "Oak St", "Maple St", "Pine St", "Cedar St", "Spruce St", "Fir St", "Cypress St");
    private static final List<String> CITY_NAMES = List.of("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose");
    private static final List<String> STATE_NAMES = List.of("NY", "CA", "IL", "TX", "AZ", "PA", "TX", "CA", "TX", "CA");
    /**
     * An immutable list of country names that can be safely used without concerns about mutability.
     */
    public static final List<String> COUNTRY_NAMES = List.of("France", "US", "UK", "Tuvalu", "Lesotho", "Kyrgyzstan", "Nepal", "Luxembourg", "Dominica", "Martinica");
    private static final List<String> ZIP_CODES = List.of("10001", "90001", "60001", "77001", "85001", "19101", "78201", "92101", "75201", "95101");
    public static final int NUM_EMPLOYEES = 500;

    private final Random random = ThreadLocalRandom.current();

    /**
     * Process the employee onboarding request with updated document structure
     */
        @Transactional
        public Employee onboard(OnboardEmployeeDTO dto) {
            log.debug("onboard method invoked");
    
            // Check if employee is rejoining
            Employee existingEmployee = employeeRepository.findEmployeeByPhoneNumber(dto.getPhoneNumber())
                    .orElse(null);
            String managerId = dto.getManagerId();
            Employee manager = managerId != null
                    ? employeeRepository.findById(new ObjectId(managerId)).orElse(null)
                    : null;
    
            if (existingEmployee != null) {
                // Update existing employee record
                log.info("Employee is rejoining, updating record with ID: {}", existingEmployee.getId());
                
                // Update job info
                Employee.JobInfo jobInfo = existingEmployee.getJobInfo();
                if (jobInfo == null) {
                    jobInfo = new Employee.JobInfo();
                }
    
                jobInfo = fillJobInfo(jobInfo, dto, manager);
                existingEmployee.setJobInfo(jobInfo);
    
                // Update address
                Employee.Address address = existingEmployee.getAddress();
                if (address == null) {
                    address = new Employee.Address();
                }
                
                address = fillAddress(address, dto);
                existingEmployee.setAddress(address);
    
                log.debug("onboard method finished");
                return employeeRepository.save(existingEmployee);
            } else {
                log.info("Creating new employee record");
                
                Employee newEmployee = new Employee();
                newEmployee.setFirstName(dto.getFirstName());
                newEmployee.setLastName(dto.getLastName());
                newEmployee.setPhoneNumber(dto.getPhoneNumber());
                
                
                Employee.JobInfo jobInfo = fillJobInfo(new Employee.JobInfo(), dto, manager);
                newEmployee.setJobInfo(jobInfo);
                
                Employee.Address address = fillAddress(new Employee.Address(), dto);
                newEmployee.setAddress(address);
                
                newEmployee = resetLeaveInfo(newEmployee);
    
                Employee result = employeeRepository.save(newEmployee);
                log.info("Employee created successfully with ID: {}", result.getId());
    
                log.debug("onboard method finished");
                return result;
            }
        }

    /**
     * Process the employee search request using Atlas Search
     */
        public List<Employee> search(SearchEmployeeByNameDTO dto) {
            log.debug("search method invoked");
            log.info("Searching for employees with name containing: {}", dto.getName());
            Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());
    
            List<Employee> directResults = employeeRepository.searchByName(dto.getName(), pageable);
            log.info("Found {} employees matching the search criteria", directResults.size());
            log.debug("search method finished");
            return directResults;
        }

        public Map<Employee, Employee> generateEmployees(int numEmployees) {
            log.debug("generateEmployees method invoked");
            Map<Employee, Employee> result = new HashMap<>();
            Employee manager = null;
            for (int i = 0; i < numEmployees; i++) {
                Employee employee = new Employee();
    
                // Random name
                employee.setFirstName(FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size())));
                employee.setLastName(LAST_NAMES.get(random.nextInt(LAST_NAMES.size())));
    
                // Random address
                Employee.Address address = getAddress();
                employee.setAddress(address);
    
                // Random phone number
                String phoneNumber = getPhoneNumber();
                employee.setPhoneNumber(phoneNumber);
    
                // Random job info
                employee = setJobInfoAndReturn(employee, manager);
    
                EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
                for (LeaveType leaveType : LeaveType.values()) {
                    leaveInfo.put(leaveType, random.nextInt(31));
                }
                employee.setLeaveInfo(leaveInfo);
    
                employee = employeeRepository.save(employee);
    
                result.put(employee, manager);
                manager = employee;
            }
            log.debug("generateEmployees method finished");
            return result;
        }

        private Employee setJobInfoAndReturn(Employee employee, Employee manager) {
            log.debug("setJobInfoAndReturn method invoked");
            Employee.JobInfo jobInfo = new Employee.JobInfo();
            jobInfo.setEmail(
                    employee.getFirstName().toLowerCase() + "." + employee.getLastName().toLowerCase() + "@em.com");
            jobInfo.setHireDate(LocalDate.now());
            jobInfo.setJobId(String.valueOf(random.nextInt(1000)));
            jobInfo.setSalary(random.nextInt(100000));
            jobInfo.setManager(manager);
            employee.setJobInfo(jobInfo);
            log.debug("setJobInfoAndReturn method finished");
            return employee;
        }

        private Employee.Address getAddress() {
            log.debug("getAddress method invoked");
            Employee.Address address = new Employee.Address();
            address.setStreet(STREET_NAMES.get(random.nextInt(STREET_NAMES.size())));
            address.setCity(CITY_NAMES.get(random.nextInt(CITY_NAMES.size())));
            address.setRegion(STATE_NAMES.get(random.nextInt(STATE_NAMES.size())));
            address.setZipCode(ZIP_CODES.get(random.nextInt(ZIP_CODES.size())));
            char blockLetter = (char) ('A' + random.nextInt(26));
            address.setBlock(String.valueOf(blockLetter) + random.nextInt(20) + 1);
            address.setBuilding(String.valueOf(random.nextInt(200) + 1));
            address.setCountry(COUNTRY_NAMES.get(random.nextInt(COUNTRY_NAMES.size())));
            address.setFloor(random.nextInt(40));
            log.debug("getAddress method finished");
            return address;
        }

        private String getPhoneNumber() {
            log.debug("getPhoneNumber method invoked");
            String phoneNumber = String.format("%03d-%03d-%04d", random.nextInt(1000), random.nextInt(1000),
                    random.nextInt(10000));
            log.debug("getPhoneNumber method finished");
            return phoneNumber;
        }

        public Employee findById(ObjectId employeeId) {
            log.debug("findById method invoked");
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with ID: " + employeeId));
            log.debug("findById method finished");
            return employee;
        }

        public Employee verifyUserAndItsManagerAndApprover(CaptureLeaveDTO dto) {
            log.debug("verifyUserAndItsManagerAndApprover method invoked");
            Employee employee = findById(new ObjectId(dto.getEmployeeId()));
    
            ObjectId approvedById = new ObjectId(dto.getApprovedById());
            Employee manager = employee.getJobInfo().getManager();
            ObjectId managerId = manager != null ? manager.getId() : employee.getId();
            if (!managerId.equals(approvedById)) {
                throw new LeaveNotApprovedByManagerException("Manager: " + managerId + " is different from the one " +
                        "who approved leave: " + approvedById);
            }
    
            log.debug("verifyUserAndItsManagerAndApprover method finished");
            return employee;
        }

        public void updateLeaveInfo(Employee employee, Map.Entry<Leave, Integer> leaveEntry) {
            log.debug("updateLeaveInfo method invoked");
            employee.getLeaveInfo().put(leaveEntry.getKey().getLeaveType(), leaveEntry.getValue());
            employeeRepository.save(employee);
            log.debug("updateLeaveInfo method finished");
        }

    public List<String> getLastNames() {
        return new ArrayList<>(LAST_NAMES);
    }

    public List<String> getStreetNames() {
        return Collections.unmodifiableList(STREET_NAMES);
    }

    public List<String> getCityNames() {
        return Collections.unmodifiableList(CITY_NAMES);
    }

    public List<String> getStateNames() {
        return Collections.unmodifiableList(STATE_NAMES);
    }

    public List<String> getZipCodes() {
        return Collections.unmodifiableList(ZIP_CODES);
    }


}