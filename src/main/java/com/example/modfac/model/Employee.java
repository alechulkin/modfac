package com.example.modfac.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;
import java.util.EnumMap;

@Document(collection = "employees")
@Data
public class Employee {
    
    @Id
    private ObjectId id;
    
    @Field(name = "FIRST_NAME")
    @Size(max = 50)
    @NotBlank
    private String firstName;
    
    @Field(name = "LAST_NAME")
    @Size(max = 50)
    @NotBlank
    private String lastName;
    
    // Embedded address document
    @Field(name = "ADDRESS")
    private Address address;
    
    @Field(name = "PHONE_NUMBER")
    @Size(max = 20)
    @NotBlank
    @Indexed(unique = true)
    private String phoneNumber;
    
    // Embedded job info document
    @Field(name = "JOB_INFO")
    private JobInfo jobInfo;
    
    // Embedded leave info map
    @Field(name = "LEAVE_INFO")
    private EnumMap<LeaveType, Integer> leaveInfo;
    
    // Nested document classes
    @Data
    public static class Address {
        @Field(name = "COUNTRY")
        @Size(max = 50)
        @NotBlank
        private String country;

        @Field(name = "REGION")
        @Size(max = 50)
        @NotBlank
        private String region;

        @Field(name = "STREET")
        @Size(max = 100)
        @NotBlank
        private String street;
        
        @Field(name = "CITY")
        @Size(max = 50)
        @NotBlank
        private String city;

        @Field(name = "BLOCK")
        @Size(max = 10)
        private String block;

        @Field(name = "BUILDING")
        @Size(max = 10)
        private String building;

        @Field(name = "APARTMENT")
        @Size(max = 10)
        @NotBlank
        private String apartment;

        @Field(name = "FLOOR")
        @Size(max = 10)
        @NotBlank
        private Integer floor;

        @Field(name = "ZIP_CODE")
        @Size(max = 10)
        @NotBlank
        private String zipCode;
    }
    
    @Data
    public static class JobInfo {
        @Field(name = "EMAIL")
        @Size(max = 100)
        @NotBlank
        private String email;
        
        @Field(name = "HIRE_DATE")
        private LocalDate hireDate;
        
        @Field(name = "JOB_ID")
        @Size(max = 10)
        @NotBlank
        private String jobId;
        
        @Field(name = "SALARY")
        private Integer salary;
        
        @Field(name = "MANAGER")
        @DBRef
        private Employee manager;  // ObjectId of manager employee
    }
}
