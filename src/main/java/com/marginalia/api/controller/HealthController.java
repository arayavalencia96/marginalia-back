package com.marginalia.api.controller;

import com.marginalia.api.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Provides the public API health-check endpoint. */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Reports whether the API process is running.
     *
     * @return a response whose status is {@code UP}
     */
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}
