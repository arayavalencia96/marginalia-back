package com.marginalia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Accepts an email change with password confirmation.
 *
 * @param newEmail replacement email address
 * @param password current account password
 */
public record ChangeEmailRequest(
        @NotBlank @Email @Size(max = 320) String newEmail,
        @NotBlank String password
) {
}
