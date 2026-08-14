package com.marginalia.api.dto;

/**
 * Returns a newly issued access token.
 *
 * @param accessToken short-lived JWT access token
 */
public record RefreshResponse(String accessToken) {
}
