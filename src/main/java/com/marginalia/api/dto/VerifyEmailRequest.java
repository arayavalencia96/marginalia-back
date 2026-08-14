package com.marginalia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Accepts data required to verify an email address.
 *
 * @param email account email address
 * @param code six-digit verification code
 */
public record VerifyEmailRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Pattern(regexp = "\\d{6}") String code
) {
}
