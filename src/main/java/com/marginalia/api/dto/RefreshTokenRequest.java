package com.marginalia.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Accepts a refresh token for refresh or logout operations.
 *
 * @param refreshToken raw opaque refresh token
 */
public record RefreshTokenRequest(
        @NotBlank @Size(max = 512) String refreshToken
) {
}
