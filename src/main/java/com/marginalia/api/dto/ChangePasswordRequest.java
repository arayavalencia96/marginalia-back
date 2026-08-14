package com.marginalia.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Accepts a password change request.
 *
 * @param currentPassword current account password
 * @param newPassword replacement password
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {
}
