package com.marginalia.api.controller;

import com.marginalia.api.dto.LoginRequest;
import com.marginalia.api.dto.LoginResponse;
import com.marginalia.api.dto.RefreshResponse;
import com.marginalia.api.dto.RefreshTokenRequest;
import com.marginalia.api.dto.RegisterRequest;
import com.marginalia.api.dto.RegisterResponse;
import com.marginalia.api.dto.VerifyEmailRequest;
import com.marginalia.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
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
     * @return the issued authentication tokens
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
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
     * Exchanges a valid refresh token for a new access token.
     *
     * @param request validated refresh-token request
     * @return a response containing the new access token
     */
    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    /**
     * Logs out a session by revoking its refresh token.
     *
     * @param request validated refresh-token request
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }
}
