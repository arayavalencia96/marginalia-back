package com.marginalia.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configures failed-login lockout behavior.
 *
 * @param maxAttempts number of failed attempts that locks an email address
 * @param lockDuration duration for which the failed-attempt counter is retained
 */
@ConfigurationProperties(prefix = "security.login-attempts")
public record LoginAttemptProperties(
        int maxAttempts,
        Duration lockDuration
) {
}
