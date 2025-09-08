package com.example.modfac.config;

import com.example.modfac.service.DataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("mongoDBIndexConfig")
@RequiredArgsConstructor
public class MongoDBDataConfig {

    private final DataService dataService;

    @Value("${data.generate}")
    private Boolean generateData;

    @PostConstruct
    public void generateData() {
        if (Boolean.TRUE.equals(generateData)) {
            dataService.generateData();
        }

    }
}
