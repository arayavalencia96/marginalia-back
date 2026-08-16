package com.marginalia.api.service;

import com.marginalia.api.config.FrontendProperties;
import com.marginalia.api.domain.PasswordResetToken;
import com.marginalia.api.domain.User;
import com.marginalia.api.exception.InvalidPasswordResetTokenException;
import com.marginalia.api.exception.NewPasswordMatchesCurrentException;
import com.marginalia.api.repository.PasswordResetTokenRepository;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.PasswordResetProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordPolicy passwordPolicy;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                passwordResetTokenRepository,
                userRepository,
                passwordEncoder,
                passwordPolicy,
                refreshTokenService,
                emailService,
                new PasswordResetProperties(Duration.ofMinutes(15)),
                new FrontendProperties("https://marginalia.example")
        );
    }

    @Test
    void requestsResetForAnActivePasswordAccount() {
        User user = user();
        when(userRepository.findByEmailAndDeletedAtIsNull("reader@example.com")).thenReturn(Optional.of(user));

        service.requestReset(" READER@example.com ");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).invalidateUnusedForUser(user.getId());
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendPasswordResetLink(eq(user.getEmail()), contains("/reset-password?token="));
        assertThat(tokenCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(tokenCaptor.getValue().getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void doesNothingForAnUnknownEmail() {
        when(userRepository.findByEmailAndDeletedAtIsNull(any())).thenReturn(Optional.empty());

        service.requestReset("unknown@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetLink(any(), any());
    }

    @Test
    void resetsPasswordAndRevokesAllSessions() {
        User user = user();
        PasswordResetToken token = PasswordResetToken.builder()
                .userId(user.getId())
                .expiresAt(Instant.now().plusSeconds(60))
                .used(false)
                .build();
        when(passwordResetTokenRepository.findByTokenHashAndUsedFalse(any())).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.resetPassword("raw-token", "new-password");

        assertThat(token.isUsed()).isTrue();
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(passwordResetTokenRepository).invalidateUnusedForUser(user.getId());
        verify(refreshTokenService).revokeAllForUser(user.getId());
    }

    @Test
    void rejectsExpiredInvalidAndUnchangedPasswordResets() {
        when(passwordResetTokenRepository.findByTokenHashAndUsedFalse(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resetPassword("missing", "new-password"))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        User user = user();
        PasswordResetToken expired = PasswordResetToken.builder()
                .userId(user.getId())
                .expiresAt(Instant.now().minusSeconds(1))
                .used(false)
                .build();
        when(passwordResetTokenRepository.findByTokenHashAndUsedFalse(any())).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.resetPassword("expired", "new-password"))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        PasswordResetToken valid = PasswordResetToken.builder()
                .userId(user.getId())
                .expiresAt(Instant.now().plusSeconds(60))
                .used(false)
                .build();
        when(passwordResetTokenRepository.findByTokenHashAndUsedFalse(any())).thenReturn(Optional.of(valid));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("same-password", "old-hash")).thenReturn(true);
        assertThatThrownBy(() -> service.resetPassword("valid", "same-password"))
                .isInstanceOf(NewPasswordMatchesCurrentException.class);
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("reader@example.com")
                .passwordHash("old-hash")
                .enabled(true)
                .build();
    }
}
