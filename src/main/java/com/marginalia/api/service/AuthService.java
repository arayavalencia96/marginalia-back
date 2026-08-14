package com.marginalia.api.service;

import com.marginalia.api.domain.User;
import com.marginalia.api.dto.LoginRequest;
import com.marginalia.api.dto.LoginResponse;
import com.marginalia.api.dto.RefreshResponse;
import com.marginalia.api.dto.RegisterRequest;
import com.marginalia.api.dto.RegisterResponse;
import com.marginalia.api.exception.EmailAlreadyExistsException;
import com.marginalia.api.exception.EmailNotVerifiedException;
import com.marginalia.api.exception.InvalidCredentialsException;
import com.marginalia.api.exception.InvalidVerificationCodeException;
import com.marginalia.api.exception.LoginLockedException;
import com.marginalia.api.exception.UsernameAlreadyExistsException;
import com.marginalia.api.repository.UserRepository;
import com.marginalia.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Coordinates password-based registration, login, email verification, and token lifecycle operations. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationCodeService verificationCodeService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordPolicy passwordPolicy;

    /**
     * Registers a disabled account and issues its initial email verification code.
     *
     * @param request registration details
     * @return public details of the newly registered account
     * @throws EmailAlreadyExistsException if the normalized email is already registered
     * @throws UsernameAlreadyExistsException if the username is already registered
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        passwordPolicy.validate(request.password());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = User.builder()
                .email(email)
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .enabled(false)
                .build();
        User savedUser = userRepository.save(user);
        verificationCodeService.issue(savedUser);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.isEnabled()
        );
    }

    /**
     * Authenticates a verified password-based account and issues access and refresh tokens.
     *
     * @param request login credentials
     * @return issued access and refresh tokens
     * @throws LoginLockedException if the email is temporarily locked after repeated failures
     * @throws InvalidCredentialsException if the credentials are invalid
     * @throws EmailNotVerifiedException if the account has not completed email verification
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        if (loginAttemptService.isLocked(email)) {
            throw new LoginLockedException();
        }

        if (passwordPolicy.exceedsBcryptLimit(request.password())) {
            rejectInvalidLogin(email);
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        if (user == null) {
            rejectInvalidLogin(email);
        }

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            rejectInvalidLogin(email);
        }
        if (!user.isEnabled()) {
            throw new EmailNotVerifiedException();
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        loginAttemptService.reset(email);
        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * Validates a refresh token and issues a new access token for its user.
     *
     * @param refreshToken raw refresh token
     * @return a response containing a new access token
     * @throws com.marginalia.api.exception.InvalidRefreshTokenException if the token is invalid, expired, or revoked
     */
    @Transactional(readOnly = true)
    public RefreshResponse refresh(String refreshToken) {
        User user = refreshTokenService.validateAndGetUser(refreshToken);
        return new RefreshResponse(jwtService.generateAccessToken(user));
    }

    /**
     * Revokes a refresh token to end its session.
     *
     * @param refreshToken raw refresh token to revoke
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    /**
     * Consumes a valid verification code and enables the corresponding account.
     *
     * @param email account email address
     * @param code six-digit verification code
     * @throws InvalidVerificationCodeException if the email or code is invalid, used, or expired
     */
    @Transactional
    public void verify(String email, String code) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(normalizeEmail(email))
                .orElseThrow(InvalidVerificationCodeException::new);
        verificationCodeService.consume(user, code);
        user.setEnabled(true);
        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void rejectInvalidLogin(String email) {
        long attempts = loginAttemptService.recordFailure(email);
        if (attempts >= loginAttemptService.maxAttempts()) {
            throw new LoginLockedException();
        }
        throw new InvalidCredentialsException();
    }
}
