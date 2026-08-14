package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.dto.LoginResponse;
import com.marginalia.api.exception.AccountDeletedException;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/** Converts a trusted OAuth email identity into a local account and the standard JWT token pair. */
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private static final int USERNAME_BASE_MAX_LENGTH = 40;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationCodeService verificationCodeService;
    private final LoginAttemptService loginAttemptService;

    /**
     * Logs in an OAuth identity, creating and enabling its local account when necessary.
     *
     * @param oauthEmail verified email supplied by the OAuth provider
     * @return issued access and refresh tokens
     * @throws AccountDeletedException if the email belongs to a soft-deleted account
     */
    @Transactional
    public LoginResponse login(String oauthEmail) {
        String email = oauthEmail.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email).orElseGet(() -> createUser(email));

        if (user.getDeletedAt() != null) {
            throw new AccountDeletedException();
        }

        if (!user.isEnabled()) {
            user.setEnabled(true);
            user = userRepository.save(user);
            verificationCodeService.invalidateForUser(user.getId());
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        loginAttemptService.reset(email);
        return new LoginResponse(accessToken, refreshToken);
    }

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .username(generateUsername(email))
                .passwordHash(null)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private String generateUsername(String email) {
        String localPart = email.substring(0, email.indexOf('@'))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
        String base = localPart.isBlank() ? "user" : localPart;
        if (base.length() > USERNAME_BASE_MAX_LENGTH) {
            base = base.substring(0, USERNAME_BASE_MAX_LENGTH);
        }
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
