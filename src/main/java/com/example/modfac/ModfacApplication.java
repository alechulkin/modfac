package com.example.modfac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.modfac")
@EnableMongoRepositories
public class ModfacApplication {
    private static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ModfacApplication.class);

	public static void main(String[] args) {
    		LOGGER.debug("main method invoked");
    		SpringApplication.run(ModfacApplication.class, args);
    		LOGGER.debug("main method finished");
    	}

}
