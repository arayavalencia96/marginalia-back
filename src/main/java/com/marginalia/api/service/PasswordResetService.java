package com.marginalia.api.service;

import com.marginalia.api.config.FrontendProperties;
import com.marginalia.api.domain.PasswordResetToken;
import com.marginalia.api.domain.User;
import com.marginalia.api.exception.InvalidPasswordResetTokenException;
import com.marginalia.api.exception.NewPasswordMatchesCurrentException;
import com.marginalia.api.repository.PasswordResetTokenRepository;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.PasswordResetProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

/** Issues and consumes short-lived, one-time password reset tokens. */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final PasswordResetProperties passwordResetProperties;
    private final FrontendProperties frontendProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates and sends a reset link when the email belongs to an active password account.
     *
     * @param email email address supplied by the requester
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(normalizeEmail(email))
                .filter(user -> user.getPasswordHash() != null)
                .ifPresent(this::issueAndSend);
    }

    /**
     * Replaces the password associated with a valid one-time token and revokes all sessions.
     *
     * @param rawToken token received from the reset link
     * @param newPassword validated replacement password
     * @throws InvalidPasswordResetTokenException if the token is invalid, expired, or cannot be used
     * @throws NewPasswordMatchesCurrentException if the new password matches the existing password
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        passwordPolicy.validate(newPassword);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashAndUsedFalse(hash(rawToken))
                .orElseThrow(InvalidPasswordResetTokenException::new);
        if (!resetToken.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = userRepository.findById(resetToken.getUserId())
                .filter(candidate -> candidate.getDeletedAt() == null && candidate.getPasswordHash() != null)
                .orElseThrow(InvalidPasswordResetTokenException::new);
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new NewPasswordMatchesCurrentException();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.setUsed(true);
        userRepository.save(user);
        passwordResetTokenRepository.invalidateUnusedForUser(user.getId());
        refreshTokenService.revokeAllForUser(user.getId());
    }

    private void issueAndSend(User user) {
        passwordResetTokenRepository.invalidateUnusedForUser(user.getId());
        String rawToken = generateToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plus(passwordResetProperties.tokenExpiration()))
                .used(false)
                .build());
        emailService.sendPasswordResetLink(user.getEmail(), buildResetUrl(rawToken));
    }

    private String buildResetUrl(String token) {
        return UriComponentsBuilder.fromUriString(frontendProperties.frontendUrl())
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
