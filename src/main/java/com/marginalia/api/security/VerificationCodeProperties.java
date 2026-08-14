package com.marginalia.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configures email verification code expiration.
 *
 * @param codeExpiration lifetime of an issued verification code
 */
@ConfigurationProperties(prefix = "security.verification")
public record VerificationCodeProperties(Duration codeExpiration) {
}
