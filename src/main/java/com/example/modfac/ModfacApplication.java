package com.example.modfac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.modfac")
@EnableMongoRepositories
public class ModfacApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModfacApplication.class, args);
	}

}
