package com.marginalia.api.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
