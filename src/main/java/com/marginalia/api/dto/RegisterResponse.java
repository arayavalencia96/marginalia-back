package com.marginalia.api.dto;

import java.util.UUID;

/**
 * Returns public account details after registration.
 *
 * @param id user identifier
 * @param email registered email address
 * @param username registered username
 * @param enabled whether email verification is complete
 */
public record RegisterResponse(
        UUID id,
        String email,
        String username,
        boolean enabled
) {
}
