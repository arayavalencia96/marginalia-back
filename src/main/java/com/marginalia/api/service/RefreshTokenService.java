package com.marginalia.api.service;

import com.marginalia.api.domain.RefreshToken;
import com.marginalia.api.domain.User;
import com.marginalia.api.exception.EmailNotVerifiedException;
import com.marginalia.api.exception.InvalidRefreshTokenException;
import com.marginalia.api.repository.RefreshTokenRepository;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/** Issues, hashes, validates, and revokes long-lived refresh tokens. */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Issues a cryptographically random refresh token and stores only its hash.
     *
     * @param user user receiving the refresh token
     * @return raw refresh token to return to the client
     */
    @Transactional
    public String issue(User user) {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plus(jwtProperties.refreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    /**
     * Validates a refresh token and retrieves its active, verified user.
     *
     * @param rawToken raw refresh token supplied by the client
     * @return active user associated with the token
     * @throws InvalidRefreshTokenException if the token is unknown, revoked, expired, or belongs to a deleted user
     * @throws EmailNotVerifiedException if the associated user is not enabled
     */
    @Transactional(readOnly = true)
    public User validateAndGetUser(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isRevoked() || !refreshToken.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);
        if (user.getDeletedAt() != null) {
            throw new InvalidRefreshTokenException();
        }
        if (!user.isEnabled()) {
            throw new EmailNotVerifiedException();
        }
        return user;
    }

    /**
     * Revokes a refresh token when it exists.
     *
     * @param rawToken raw refresh token supplied by the client
     */
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    /**
     * Revokes every refresh token issued to a user.
     *
     * @param userId identifier of the user whose sessions must be revoked
     */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
