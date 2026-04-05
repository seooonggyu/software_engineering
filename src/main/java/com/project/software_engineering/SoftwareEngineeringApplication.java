package com.project.software_engineering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SoftwareEngineeringApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftwareEngineeringApplication.class, args);
    }

}
