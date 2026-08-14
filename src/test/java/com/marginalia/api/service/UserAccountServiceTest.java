package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.dto.ChangeEmailRequest;
import com.marginalia.api.dto.ChangePasswordRequest;
import com.marginalia.api.dto.ChangeUsernameRequest;
import com.marginalia.api.dto.DeleteAccountRequest;
import com.marginalia.api.exception.EmailAlreadyExistsException;
import com.marginalia.api.exception.InvalidCredentialsException;
import com.marginalia.api.exception.NewPasswordMatchesCurrentException;
import com.marginalia.api.exception.PasswordAuthenticationUnavailableException;
import com.marginalia.api.exception.UserNotFoundException;
import com.marginalia.api.exception.UsernameAlreadyExistsException;
import com.marginalia.api.repository.UserRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordPolicy passwordPolicy;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UserAccountService service;

    @Test
    void changesPasswordAndRevokesSessions() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword(user.getId(), new ChangePasswordRequest("current", "new-password"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(refreshTokenService).revokeAllForUser(user.getId());
    }

    @Test
    void rejectsSameNewPasswordAndInvalidConfirmation() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("same", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(user.getId(), new ChangePasswordRequest("current", "same")))
                .isInstanceOf(NewPasswordMatchesCurrentException.class);

        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);
        assertThatThrownBy(() -> service.deleteAccount(user.getId(), new DeleteAccountRequest("wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void changesEmailAndStartsVerificationFlow() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        service.changeEmail(user.getId(), new ChangeEmailRequest(" NEW@example.com ", "current"));

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.isEnabled()).isFalse();
        verify(verificationCodeService).issue(user);
        verify(refreshTokenService).revokeAllForUser(user.getId());
    }

    @Test
    void rejectsDuplicateEmailAndUsername() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.changeEmail(
                user.getId(),
                new ChangeEmailRequest("used@example.com", "current")
        )).isInstanceOf(EmailAlreadyExistsException.class);

        when(userRepository.existsByUsername("used-name")).thenReturn(true);
        assertThatThrownBy(() -> service.changeUsername(user.getId(), new ChangeUsernameRequest("used-name")))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void changesUsernameAndSoftDeletesAccount() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);

        service.changeUsername(user.getId(), new ChangeUsernameRequest("new-name"));
        assertThat(user.getUsername()).isEqualTo("new-name");

        service.deleteAccount(user.getId(), new DeleteAccountRequest("current"));
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isEnabled()).isFalse();
        verify(verificationCodeService).invalidateForUser(user.getId());
    }

    @Test
    void rejectsMissingAndOauthOnlyUsers() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.changeUsername(missingId, new ChangeUsernameRequest("name")))
                .isInstanceOf(UserNotFoundException.class);

        User oauthUser = user();
        oauthUser.setPasswordHash(null);
        when(userRepository.findById(oauthUser.getId())).thenReturn(Optional.of(oauthUser));
        assertThatThrownBy(() -> service.deleteAccount(oauthUser.getId(), new DeleteAccountRequest("password")))
                .isInstanceOf(PasswordAuthenticationUnavailableException.class);
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("old@example.com")
                .username("reader")
                .passwordHash("old-hash")
                .enabled(true)
                .build();
    }
}
