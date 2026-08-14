package com.marginalia.api.service;

import com.marginalia.api.domain.RefreshToken;
import com.marginalia.api.domain.User;
import com.marginalia.api.exception.EmailNotVerifiedException;
import com.marginalia.api.exception.InvalidRefreshTokenException;
import com.marginalia.api.repository.RefreshTokenRepository;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(
                tokenRepository,
                userRepository,
                new JwtProperties("secret", Duration.ofMinutes(15), Duration.ofDays(7))
        );
    }

    @Test
    void issuesRandomTokenAndStoresOnlyHash() {
        User user = User.builder().id(UUID.randomUUID()).build();

        String rawToken = service.issue(user);

        assertThat(rawToken).isNotBlank();
        verify(tokenRepository).save(org.mockito.ArgumentMatchers.argThat(token ->
                token.getUserId().equals(user.getId())
                        && token.getTokenHash().length() == 64
                        && !token.getTokenHash().equals(rawToken)));
    }

    @Test
    void validatesActiveTokenForEnabledUser() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = token(userId, false, Instant.now().plusSeconds(60));
        User user = User.builder().id(userId).enabled(true).build();
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThat(service.validateAndGetUser("raw")).isSameAs(user);
    }

    @Test
    void rejectsUnknownRevokedExpiredAndDeletedTokens() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.validateAndGetUser("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        UUID userId = UUID.randomUUID();
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token(userId, true, Instant.now().plusSeconds(60))));
        assertThatThrownBy(() -> service.validateAndGetUser("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token(userId, false, Instant.now().minusSeconds(1))));
        assertThatThrownBy(() -> service.validateAndGetUser("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token(userId, false, Instant.now().plusSeconds(60))));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).deletedAt(Instant.now()).build()));
        assertThatThrownBy(() -> service.validateAndGetUser("raw")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsDisabledUser() {
        UUID userId = UUID.randomUUID();
        when(tokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(token(userId, false, Instant.now().plusSeconds(60))));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).enabled(false).build()));

        assertThatThrownBy(() -> service.validateAndGetUser("raw")).isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void revokesOneOrAllTokens() {
        RefreshToken token = token(UUID.randomUUID(), false, Instant.now().plusSeconds(60));
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        service.revoke("raw");
        assertThat(token.isRevoked()).isTrue();
        verify(tokenRepository).save(token);

        service.revokeAllForUser(token.getUserId());
        verify(tokenRepository).revokeAllByUserId(token.getUserId());
    }

    private RefreshToken token(UUID userId, boolean revoked, Instant expiresAt) {
        return RefreshToken.builder().userId(userId).revoked(revoked).expiresAt(expiresAt).build();
    }
}
