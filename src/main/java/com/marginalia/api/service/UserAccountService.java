package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.dto.ChangeEmailRequest;
import com.marginalia.api.dto.ChangePasswordRequest;
import com.marginalia.api.dto.ChangeUsernameRequest;
import com.marginalia.api.dto.DeleteAccountRequest;
import com.marginalia.api.dto.UserProfileResponse;
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

/** Implements authenticated changes to account credentials, identity fields, and deletion state. */
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final VerificationCodeService verificationCodeService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    /**
     * Returns the current profile for an active user.
     *
     * @param userId identifier of the user
     * @return current profile and credential capabilities
     * @throws UserNotFoundException if the active user does not exist
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return UserProfileResponse.from(requireUser(userId));
    }

    /**
     * Changes a local account password and revokes its existing sessions.
     *
     * @param userId identifier of the user
     * @param request current-password confirmation and new password
     * @throws UserNotFoundException if the active user does not exist
     * @throws InvalidCredentialsException if the current password is invalid
     * @throws PasswordAuthenticationUnavailableException if the OAuth-only account has no local password
     * @throws NewPasswordMatchesCurrentException if the new password matches the current password
     */
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

    /**
     * Changes an account email, disables the account, and starts a new verification flow.
     *
     * @param userId identifier of the user
     * @param request password confirmation and new email address
     * @throws UserNotFoundException if the active user does not exist
     * @throws InvalidCredentialsException if the password confirmation is invalid
     * @throws PasswordAuthenticationUnavailableException if the OAuth-only account has no local password
     * @throws EmailAlreadyExistsException if the new email is already registered
     */
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

    /**
     * Changes an account's unique username.
     *
     * @param userId identifier of the user
     * @param request new username
     * @throws UserNotFoundException if the active user does not exist
     * @throws UsernameAlreadyExistsException if the username is already registered
     */
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

    /**
     * Soft-deletes an account and revokes its tokens and verification codes.
     *
     * @param userId identifier of the user
     * @param request password confirmation when the account has a local password
     * @throws UserNotFoundException if the active user does not exist
     * @throws InvalidCredentialsException if the password confirmation is invalid
     */
    @Transactional
    public void deleteAccount(UUID userId, DeleteAccountRequest request) {
        User user = requireUser(userId);
        if (user.getPasswordHash() != null) {
            validatePasswordConfirmation(request.password(), user);
        }

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
        if (password == null
                || password.isBlank()
                || passwordPolicy.exceedsBcryptLimit(password)
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
