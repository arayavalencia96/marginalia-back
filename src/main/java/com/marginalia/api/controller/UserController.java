package com.marginalia.api.controller;

import com.marginalia.api.dto.ChangeEmailRequest;
import com.marginalia.api.dto.ChangePasswordRequest;
import com.marginalia.api.dto.ChangeUsernameRequest;
import com.marginalia.api.dto.DeleteAccountRequest;
import com.marginalia.api.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService userAccountService;

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userAccountService.changePassword(userId, request);
    }

    @PatchMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangeEmailRequest request
    ) {
        userAccountService.changeEmail(userId, request);
    }

    @PatchMapping("/username")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUsername(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangeUsernameRequest request
    ) {
        userAccountService.changeUsername(userId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody DeleteAccountRequest request
    ) {
        userAccountService.deleteAccount(userId, request);
    }
}
