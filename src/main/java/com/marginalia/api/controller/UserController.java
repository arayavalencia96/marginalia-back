package com.marginalia.api.controller;

import com.marginalia.api.dto.ChangeEmailRequest;
import com.marginalia.api.dto.ChangePasswordRequest;
import com.marginalia.api.dto.ChangeUsernameRequest;
import com.marginalia.api.dto.DeleteAccountRequest;
import com.marginalia.api.dto.UserProfileResponse;
import com.marginalia.api.service.UserAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Exposes authenticated self-service endpoints for account credentials and account deletion. */
@RestController
@RequestMapping("/api/users/me")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService userAccountService;

    /**
     * Returns the authenticated user's current profile.
     *
     * @param userId identifier of the authenticated user
     * @return current profile and credential capabilities
     */
    @GetMapping
    public UserProfileResponse getProfile(@AuthenticationPrincipal UUID userId) {
        return userAccountService.getProfile(userId);
    }

    /**
     * Changes the authenticated user's password after verifying the current password.
     *
     * @param userId identifier of the authenticated user
     * @param request validated current and new passwords
     */
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userAccountService.changePassword(userId, request);
    }

    /**
     * Changes the authenticated user's email and starts a new verification flow.
     *
     * @param userId identifier of the authenticated user
     * @param request validated password confirmation and new email
     */
    @PatchMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangeEmailRequest request
    ) {
        userAccountService.changeEmail(userId, request);
    }

    /**
     * Changes the authenticated user's unique username.
     *
     * @param userId identifier of the authenticated user
     * @param request validated username data
     */
    @PatchMapping("/username")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUsername(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangeUsernameRequest request
    ) {
        userAccountService.changeUsername(userId, request);
    }

    /**
     * Soft-deletes the authenticated account after the applicable confirmation.
     *
     * @param userId identifier of the authenticated user
     * @param request optional password confirmation for OAuth-only accounts
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody DeleteAccountRequest request
    ) {
        userAccountService.deleteAccount(userId, request);
    }
}
