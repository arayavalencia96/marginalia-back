package com.marginalia.api.dto;

/**
 * Reports API process health.
 *
 * @param status current health status
 */
public record HealthResponse(String status) {
}
