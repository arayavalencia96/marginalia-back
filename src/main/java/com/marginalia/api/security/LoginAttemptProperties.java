package com.marginalia.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.login-attempts")
public record LoginAttemptProperties(
        int maxAttempts,
        Duration lockDuration
) {
}
