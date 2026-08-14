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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final VerificationCodeService verificationCodeService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = requireUser(userId);
        validatePasswordConfirmation(request.currentPassword(), user);
        passwordPolicy.validate(request.newPassword());
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new NewPasswordMatchesCurrentException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(userId);
    }

    @Transactional
    public void changeEmail(UUID userId, ChangeEmailRequest request) {
        User user = requireUser(userId);
        validatePasswordConfirmation(request.password(), user);

        String previousEmail = user.getEmail();
        String newEmail = normalizeEmail(request.newEmail());
        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException();
        }

        user.setEmail(newEmail);
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        verificationCodeService.issue(savedUser);
        refreshTokenService.revokeAllForUser(userId);
        loginAttemptService.reset(previousEmail);
        loginAttemptService.reset(newEmail);
    }

    @Transactional
    public void changeUsername(UUID userId, ChangeUsernameRequest request) {
        User user = requireUser(userId);
        String username = request.username().trim();
        if (username.equals(user.getUsername())) {
            return;
        }
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }

        user.setUsername(username);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(UUID userId, DeleteAccountRequest request) {
        User user = requireUser(userId);
        validatePasswordConfirmation(request.password(), user);

        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(userId);
        verificationCodeService.invalidateForUser(userId);
        loginAttemptService.reset(user.getEmail());
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(UserNotFoundException::new);
    }

    private void validatePasswordConfirmation(String password, User user) {
        if (user.getPasswordHash() == null) {
            throw new PasswordAuthenticationUnavailableException();
        }
        if (passwordPolicy.exceedsBcryptLimit(password)
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
