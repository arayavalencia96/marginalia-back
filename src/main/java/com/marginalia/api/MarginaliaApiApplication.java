package com.marginalia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Bootstraps the Marginalia API and enables its scheduled maintenance jobs. */
@SpringBootApplication
@EnableScheduling
public class MarginaliaApiApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(MarginaliaApiApplication.class, args);
    }
}
