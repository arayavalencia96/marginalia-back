package com.marginalia.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Accepts password confirmation for account deletion.
 *
 * @param password current account password
 */
public record DeleteAccountRequest(
        @NotBlank String password
) {
}
