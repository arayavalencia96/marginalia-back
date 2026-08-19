package com.marginalia.api.dto;

import jakarta.validation.constraints.Size;

/**
 * Accepts optional password confirmation for account deletion.
 *
 * @param password current account password, required only for accounts with a local password
 */
public record DeleteAccountRequest(
        @Size(max = 72) String password
) {
}
