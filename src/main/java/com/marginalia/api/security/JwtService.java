package com.marginalia.api.security;

import com.marginalia.api.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** Signs short-lived JWT access tokens and validates their signatures when extracting user identities. */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    /**
     * Creates a JWT service from the configured signing secret.
     *
     * @param properties JWT signing and expiration configuration
     * @throws IllegalArgumentException if the configured secret cannot form a valid HMAC key
     */
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed access token containing the user's identifier and email.
     *
     * @param user user for whom the token is issued
     * @return compact serialized JWT access token
     */
    public String generateAccessToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.accessTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies a signed access token and extracts its user identifier.
     *
     * @param token compact serialized JWT
     * @return user identifier stored in the token subject
     * @throws io.jsonwebtoken.JwtException if the token is malformed, invalid, or cannot be verified
     * @throws IllegalArgumentException if the token subject is not a UUID
     */
    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }
}
