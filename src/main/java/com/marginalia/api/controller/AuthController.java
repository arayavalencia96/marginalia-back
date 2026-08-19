package com.marginalia.api.controller;

import com.marginalia.api.dto.LoginRequest;
import com.marginalia.api.dto.LoginResponse;
import com.marginalia.api.dto.ForgotPasswordRequest;
import com.marginalia.api.dto.RefreshResponse;
import com.marginalia.api.dto.RegisterRequest;
import com.marginalia.api.dto.RegisterResponse;
import com.marginalia.api.dto.ResetPasswordRequest;
import com.marginalia.api.dto.VerifyEmailRequest;
import com.marginalia.api.exception.InvalidRefreshTokenException;
import com.marginalia.api.security.RefreshTokenCookie;
import com.marginalia.api.service.AuthService;
import com.marginalia.api.service.PasswordResetService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Provides public endpoints for registration, authentication, verification, and token lifecycle operations. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookie refreshTokenCookie;

    /**
     * Registers a new password-based user account.
     *
     * @param request validated registration details
     * @return the registered user's public account details
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Authenticates a verified user and issues access and refresh tokens.
     *
     * @param request validated email and password credentials
     * @param response HTTP response used to set the refresh-token cookie
     * @return the issued access token
     */
    @PostMapping("/login")
    public RefreshResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResponse tokens = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(tokens.refreshToken()).toString());
        return new RefreshResponse(tokens.accessToken());
    }

    /**
     * Verifies a user's email address with a previously issued code.
     *
     * @param request validated email address and verification code
     */
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verify(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verify(request.email(), request.code());
    }

    /**
     * Starts a password reset flow without revealing whether an account exists.
     *
     * @param request validated email address
     */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
    }

    /**
     * Replaces a password using a valid, unexpired reset token.
     *
     * @param request validated reset token and new password
     */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }

    /**
     * Exchanges a valid refresh token for a new access token.
     *
     * @param refreshTokenFromCookie refresh token stored in an HttpOnly cookie
     * @return a response containing the new access token
     */
    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @CookieValue(value = RefreshTokenCookie.NAME, required = false) String refreshTokenFromCookie
    ) {
        if (refreshTokenFromCookie == null || refreshTokenFromCookie.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        return authService.refresh(refreshTokenFromCookie);
    }

    /**
     * Logs out a session by revoking its refresh token.
     *
     * @param refreshTokenFromCookie refresh token stored in an HttpOnly cookie
     * @param response HTTP response used to expire the refresh-token cookie
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(value = RefreshTokenCookie.NAME, required = false) String refreshTokenFromCookie,
            HttpServletResponse response
    ) {
        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank()) {
            authService.logout(refreshTokenFromCookie);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString());
    }
}
