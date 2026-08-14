package com.marginalia.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Accepts a username change request.
 *
 * @param username replacement unique username
 */
public record ChangeUsernameRequest(
        @NotBlank @Size(min = 3, max = 50) String username
) {
}
