package com.marginalia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Accepts password-based login credentials.
 *
 * @param email account email address
 * @param password account password
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
