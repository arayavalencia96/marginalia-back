package com.marginalia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarginaliaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarginaliaApiApplication.class, args);
    }
}
