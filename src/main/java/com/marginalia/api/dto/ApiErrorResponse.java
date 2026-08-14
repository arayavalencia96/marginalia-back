package com.marginalia.api.dto;

import java.time.Instant;

/**
 * Represents the consistent API error payload.
 *
 * @param timestamp time at which the error occurred
 * @param status HTTP status code
 * @param error HTTP error name
 * @param message human-readable error detail
 * @param path request path that produced the error
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
