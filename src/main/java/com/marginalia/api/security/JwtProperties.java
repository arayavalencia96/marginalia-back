package com.marginalia.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Holds JWT signing and expiration configuration.
 *
 * @param secret HMAC signing secret
 * @param accessTokenExpiration lifetime of an access token
 * @param refreshTokenExpiration lifetime of a refresh token
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}
