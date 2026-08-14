package com.marginalia.api.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configures the authentication endpoint rate limit.
 *
 * @param requests maximum requests allowed in one window
 * @param window duration of each rate-limit window
 */
@Validated
@ConfigurationProperties("security.auth-rate-limit")
public record AuthRateLimitProperties(
        @Min(1) long requests,
        @NotNull Duration window
) {
}
