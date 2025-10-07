package com.example.modfac.controller;

import com.example.modfac.model.Employee;
import com.example.modfac.model.LeaveType;
import com.example.modfac.model.Role;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.EnumMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.config.name=application-test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchControllerIntegrationTest extends IntegrationTestSuperclass {
    /**
     * Integration tests for the SearchController.
     *
     * This class verifies the functionality of the search API for employees, including
     * various scenarios such as valid queries, substring matches, case-insensitive searches,
     * pagination, and error handling for invalid inputs.
     *
     * The tests ensure that the search API behaves as expected and returns the correct
     * results based on the provided search criteria.
     */
    private final String SEARCH_API = "/api/search/employees";

    /**
     * Initializes the test data and environment for the integration tests.
     *
     * This method sets up the necessary data in the repositories, such as creating
     * an admin user and an employee with predefined attributes. It also ensures
     * that the environment is ready for the tests by introducing a delay to allow
     * for any asynchronous processes to complete.
     *
     * @throws Exception if an error occurs during setup.
     */
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

    /**
     * Cleans up resources and resets the state after all tests have been executed.
     *
     * This method ensures that any data or configurations modified during the tests
     * are reverted to their original state, preventing side effects on subsequent tests
     * or the application environment.
     */
    @AfterAll
    void cleanUp() {
        super.cleanUp();
    }

    /**
     * Tests the search functionality for employees by first name.
     *
     * This test verifies that the search API correctly returns employees whose
     * first name matches the provided query. It ensures that the search is
     * functioning as expected for valid input queries.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withValidQuery_shouldReturnMatchingEmployeesForFirstName() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
    
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

    /**
     * Tests the search functionality for employees using substring matches.
     *
     * This test verifies that the search API correctly returns employees whose
     * names partially match the provided query. It ensures that the search
     * functionality supports substring matching as expected.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withSubstringMatch_shouldReturnResults() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
    
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



    /**
     * Tests the search functionality for employees with typos in their last names.
     *
     * This test verifies that the search API can handle queries with minor typos
     * in the last name and still return the correct employee results. It ensures
     * that the search functionality is robust against such input variations.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withTypoInLastName_shouldReturnResults() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
    
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


    /**
     * Tests the search functionality for employees by last name.
     *
     * This test verifies that the search API correctly returns employees whose
     * last name matches the provided query. It ensures that the search is
     * functioning as expected for valid input queries.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withValidQuery_shouldReturnMatchingEmployeesForLastName() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
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

    /**
     * Tests the search functionality for employees with case-insensitive queries.
     *
     * This test verifies that the search API correctly returns employees whose
     * names match the provided query, regardless of the case of the input.
     * It ensures that the search functionality is case-insensitive as expected.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withValidQuery_shouldReturnMatchingEmployeesCaseInsensitive() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
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

    /**
     * Tests the pagination functionality of the search API for employees.
     *
     * This test verifies that the search API correctly returns paginated results
     * when provided with valid pagination parameters. It ensures that the API
     * handles pagination as expected, returning the correct number of results
     * for each page and an empty list when no more results are available.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withPagination_shouldReturnPaginatedResults() throws Exception {
        cleanUp();
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
    
        Thread.sleep(20_000);
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
    
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


    /**
     * Tests the behavior of the search API when provided with a short query.
     *
     * This test verifies that the API returns a Bad Request status when the
     * search query is too short to be valid. It ensures that the API enforces
     * input validation for query length.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withShortQuery_shouldReturnBadRequest() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
    
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


    /**
     * Tests the behavior of the search API when no matches are found.
     *
     * This test verifies that the API returns an empty list when the search query
     * does not match any employees. It ensures that the API handles such cases
     * gracefully and provides the expected response.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    //    @WithMockUser(username = ADMIN_USERNAME, authorities = {Role.ADMIN.name()})
    void searchEmployees_withNoMatch_shouldReturnEmptyList() throws Exception {
        String token = jwtTokenProvider.createToken(ADMIN_USERNAME, Role.ADMIN.name());
    
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
}

