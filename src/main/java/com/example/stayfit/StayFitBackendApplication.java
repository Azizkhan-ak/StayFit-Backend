package com.example.stayfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@SpringBootApplication
public class StayFitBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(StayFitBackendApplication.class, args);
    }
}
