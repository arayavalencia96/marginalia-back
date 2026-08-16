package com.marginalia.api.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Holds expiration settings for one-time password reset tokens. */
@Validated
@ConfigurationProperties(prefix = "security.password-reset")
public record PasswordResetProperties(@NotNull Duration tokenExpiration) {
}
