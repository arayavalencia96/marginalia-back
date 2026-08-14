package com.marginalia.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.verification")
public record VerificationCodeProperties(Duration codeExpiration) {
}
