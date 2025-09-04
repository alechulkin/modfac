package com.example.modfac.repository;

import com.example.modfac.model.Employee;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EmployeeRepository extends PagingAndSortingRepository<Employee, ObjectId> {

    @Aggregation(pipeline = {
            "{ $search: { " +
                    "   'index': 'name-search-index', " +
                    "   'compound': { " +
                    "       'should': [ " +
                    "           { 'autocomplete': { " +
                    "               'query': ?0, " +
                    "               'path': 'FIRST_NAME', " +
                    "               'fuzzy': { 'maxEdits': 1 } " +
                    "           } }, " +
                    "           { 'autocomplete': { " +
                    "               'query': ?0, " +
                    "               'path': 'LAST_NAME', " +
                    "               'fuzzy': { 'maxEdits': 1 } " +
                    "           } } " +
                    "       ] " +
                    "   } " +
                    "} }"
    })
    List<Employee> searchByName(String nameQuery, Pageable pageable);

    @Query("{ 'phoneNumber': ?0 }")
    Optional<Employee> findEmployeeByPhoneNumber(String phoneNumber);

    Employee save(Employee employee);

    Optional<Employee> findById(ObjectId id);

    void deleteAll();
}