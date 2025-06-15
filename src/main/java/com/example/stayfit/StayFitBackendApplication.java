package com.example.stayfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.example.stayfit")
public class StayFitBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(StayFitBackendApplication.class, args);
    }
}
