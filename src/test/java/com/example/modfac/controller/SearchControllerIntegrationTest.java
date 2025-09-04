package com.example.modfac.controller;

import com.example.modfac.model.Employee;
import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Role;
import com.example.modfac.model.User;
import com.example.modfac.repository.EmployeeRepository;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.security.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.EnumMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.config.name=application-test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final String SEARCH_API = "/api/search/employees";
    private final String ADMIN_USERNAME = "adminSearch";

    @BeforeAll
    void setUp() throws Exception {
        userRepository.save(createAdminUser());

        Employee employee = new Employee();
        employee.setId(new ObjectId());
        employee.setFirstName("Alice");
        employee.setLastName("Wonderland");
        employee.setPhoneNumber("+123456789");

        Employee.Address address = new Employee.Address();
        address.setCountry("USA");
        address.setRegion("CA");
        address.setStreet("123 Fictional St");
        address.setCity("Imaginaria");
        address.setBlock("B");
        address.setBuilding("1");
        address.setApartment("1A");
        address.setFloor(1);
        address.setZipCode("00000");
        employee.setAddress(address);

        Employee.JobInfo jobInfo = new Employee.JobInfo();
        jobInfo.setEmail("alice@example.com");
        jobInfo.setHireDate(LocalDate.now().minusMonths(3));
        jobInfo.setJobId("MAG001");
        jobInfo.setSalary(75000);
        jobInfo.setManager(null);
        employee.setJobInfo(jobInfo);

        EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
        leaveInfo.put(LeaveType.PTO, 10);
        leaveInfo.put(LeaveType.SICK, 5);
        employee.setLeaveInfo(leaveInfo);

        employeeRepository.save(employee);

        Thread.sleep(12000);
    }

    @AfterAll
    void cleanUp() {
        employeeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withValidQuery_shouldReturnMatchingEmployeesForFirstName() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        String requestBody = """
        {
          "name": "Alice"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withSubstringMatch_shouldReturnResults() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        String requestBody = """
        {
          "name": "Ali"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }



    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withTypoInLastName_shouldReturnResults() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        String requestBody = """
        {
          "name": "Wunder"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Wonderland"));
    }


    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withValidQuery_shouldReturnMatchingEmployeesForLastName() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");
        String requestBody = """
        {
          "name": "Wonderland"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withValidQuery_shouldReturnMatchingEmployeesCaseInsensitive() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");
        String requestBody = """
        {
          "name": "WONDERLAND"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withPagination_shouldReturnPaginatedResults() throws Exception {
        employeeRepository.deleteAll();
        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setId(new ObjectId());
            emp.setFirstName("Alice");
            emp.setLastName("Wonderland");
            emp.setPhoneNumber("+100000000" + i);

            Employee.Address address = new Employee.Address();
            address.setCountry("Wonderland");
            address.setRegion("R");
            address.setStreet("Street " + i);
            address.setCity("City");
            address.setBlock("B");
            address.setBuilding("1");
            address.setApartment("A");
            address.setFloor(1);
            address.setZipCode("12345");
            emp.setAddress(address);

            Employee.JobInfo jobInfo = new Employee.JobInfo();
            jobInfo.setEmail("alice" + i + "@example.com");
            jobInfo.setHireDate(LocalDate.now().minusDays(i));
            jobInfo.setJobId("DEV00" + i);
            jobInfo.setSalary(50000 + (i * 100));
            jobInfo.setManager(null);
            emp.setJobInfo(jobInfo);

            EnumMap<LeaveType, Integer> leaveInfo = new EnumMap<>(LeaveType.class);
            leaveInfo.put(LeaveType.PTO, 10);
            emp.setLeaveInfo(leaveInfo);

            employeeRepository.save(emp);
        }

        Thread.sleep(7000);
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        for (int page = 0; page < 4; page++) {
            int expectedSize = page < 3 ? 5 : 0;

            String requestBody = String.format("""
                {
                  "name": "Alice",
                  "page": %d,
                  "size": 5
                }
                """, page);

            mockMvc.perform(post(SEARCH_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(expectedSize));
        }
    }


    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withShortQuery_shouldReturnBadRequest() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        String requestBody = """
        {
          "name": "Al"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ADMIN"})
    void searchEmployees_withNoMatch_shouldReturnEmptyList() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, "ADMIN");

        String requestBody = """
        {
          "name": "Nobody"
        }
        """;

        mockMvc.perform(post(SEARCH_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    private User createAdminUser() {
        User user = new User();
        user.setUsername(ADMIN_USERNAME);
        user.setPassword("dummy");
        user.setRole(Role.ADMIN);
        return user;
    }
}

