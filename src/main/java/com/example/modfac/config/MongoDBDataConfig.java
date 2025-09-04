package com.example.modfac.config;

import com.example.modfac.model.*;
import com.example.modfac.repository.EmployeeRepository;
import com.example.modfac.repository.UserRepository;
import com.example.modfac.service.EmployeeService;
import com.example.modfac.service.LeaveService;
import com.example.modfac.service.MainService;
import com.example.modfac.service.UserService;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@DependsOn("mongoDBIndexConfig")
@RequiredArgsConstructor
public class MongoDBDataConfig {

    private final MainService mainService;

    @Value("${data.generate}")
    private Boolean generateData;

    @PostConstruct
    public void generateData() {
        if (Boolean.TRUE.equals(generateData)) {
            mainService.generateData();
        }

    }
}
