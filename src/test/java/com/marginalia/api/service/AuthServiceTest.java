package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.dto.LoginRequest;
import com.marginalia.api.dto.RegisterRequest;
import com.marginalia.api.exception.EmailAlreadyExistsException;
import com.marginalia.api.exception.EmailNotVerifiedException;
import com.marginalia.api.exception.InvalidCredentialsException;
import com.marginalia.api.exception.InvalidVerificationCodeException;
import com.marginalia.api.exception.LoginLockedException;
import com.marginalia.api.exception.UsernameAlreadyExistsException;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private PasswordPolicy passwordPolicy;

    @InjectMocks
    private AuthService service;

    @Test
    void registersDisabledUserAndIssuesVerificationCode() {
        RegisterRequest request = new RegisterRequest(" USER@example.com ", "reader", "password1");
        when(passwordEncoder.encode(request.password())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        var response = service.register(request);

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.enabled()).isFalse();
        verify(verificationCodeService).issue(any(User.class));
    }

    @Test
    void rejectsDuplicateRegistrationFields() {
        RegisterRequest request = new RegisterRequest("user@example.com", "reader", "password1");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(EmailAlreadyExistsException.class);

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("reader")).thenReturn(true);
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void logsInVerifiedUserAndResetsFailures() {
        String email = "user@example.com";
        User user = User.builder().id(UUID.randomUUID()).email(email).passwordHash("hash").enabled(true).build();
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(refreshTokenService.issue(user)).thenReturn("refresh");

        var response = service.login(new LoginRequest(email, "password1"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(loginAttemptService).reset(email);
    }

    @Test
    void rejectsLockedLoginBeforeCheckingCredentials() {
        when(loginAttemptService.isLocked("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "password1")))
                .isInstanceOf(LoginLockedException.class);
        verify(userRepository, never()).findByEmailAndDeletedAtIsNull(any());
    }

    @Test
    void recordsInvalidCredentialsAndLocksAtThreshold() {
        when(loginAttemptService.recordFailure("missing@example.com")).thenReturn(1L);
        when(loginAttemptService.maxAttempts()).thenReturn(5);

        assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "password1")))
                .isInstanceOf(InvalidCredentialsException.class);

        when(loginAttemptService.recordFailure("missing@example.com")).thenReturn(5L);
        assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "password1")))
                .isInstanceOf(LoginLockedException.class);
    }

    @Test
    void rejectsUnverifiedUser() {
        User user = User.builder().email("user@example.com").passwordHash("hash").enabled(false).build();
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "password1")))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void refreshLogoutAndVerifyDelegateTokenFlows() {
        User user = User.builder().id(UUID.randomUUID()).build();
        when(refreshTokenService.validateAndGetUser("refresh")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access");

        assertThat(service.refresh("refresh").accessToken()).isEqualTo("access");
        service.logout("refresh");
        verify(refreshTokenService).revoke("refresh");

        user.setEmail("user@example.com");
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.of(user));
        service.verify("USER@example.com", "123456");
        assertThat(user.isEnabled()).isTrue();
        verify(verificationCodeService).consume(user, "123456");
    }

    @Test
    void rejectsVerificationForUnknownEmail() {
        when(userRepository.findByEmailAndDeletedAtIsNull("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify("missing@example.com", "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }
}
