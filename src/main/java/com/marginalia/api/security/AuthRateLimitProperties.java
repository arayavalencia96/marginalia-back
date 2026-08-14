package com.marginalia.api.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("security.auth-rate-limit")
public record AuthRateLimitProperties(
        @Min(1) long requests,
        @NotNull Duration window
) {
}
