package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.carddemo.domain")
@EntityScan(basePackages = "com.carddemo.domain")
public class CarddemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarddemoApplication.class, args);
    }
}
