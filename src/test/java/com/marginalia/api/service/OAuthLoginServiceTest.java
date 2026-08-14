package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.exception.AccountDeletedException;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private OAuthLoginService service;

    @Test
    void createsEnabledUserAndIssuesTokens() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access");
        when(refreshTokenService.issue(any(User.class))).thenReturn("refresh");

        var response = service.login(" USER@example.com ");

        assertThat(response.accessToken()).isEqualTo("access");
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.isEnabled() && user.getPasswordHash() == null && user.getUsername().startsWith("user-")));
    }

    @Test
    void enablesExistingUserAndInvalidatesVerificationCode() {
        User user = User.builder().id(UUID.randomUUID()).email("user@example.com").enabled(false).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.login(user.getEmail());

        assertThat(user.isEnabled()).isTrue();
        verify(verificationCodeService).invalidateForUser(user.getId());
    }

    @Test
    void rejectsDeletedAccount() {
        User user = User.builder().email("user@example.com").deletedAt(Instant.now()).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(user.getEmail())).isInstanceOf(AccountDeletedException.class);
    }
}
