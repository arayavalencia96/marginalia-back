package com.marginalia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contains the email address for a password reset request. */
public record ForgotPasswordRequest(
        @NotBlank @Email @Size(max = 320) String email
) {
}
