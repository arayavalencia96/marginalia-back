package com.marginalia.api.dto;

/**
 * Returns the token pair issued after authentication.
 *
 * @param accessToken short-lived JWT access token
 * @param refreshToken long-lived opaque refresh token
 */
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
