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
    /**
     * Configuration class for MongoDB data operations.
     * <p>
     * This class is responsible for managing the generation of data in MongoDB
     * based on the application configuration. It uses the {@link DataService}
     * to perform data generation tasks when the 'data.generate' property is set to true.
     * </p>
     */

    private final DataService dataService;

    @Value("${data.generate}")
    private Boolean generateData;

    /**
         * Generates data in MongoDB if the 'data.generate' property is set to true.
         * <p>
         * This method is executed after the bean's properties have been set, and it
         * delegates the data generation task to the {@link DataService}.
         * </p>
         */
        @PostConstruct
        public void generateData() {
            if (Boolean.TRUE.equals(generateData)) {
                dataService.generateData();
            }
    
        }
}
