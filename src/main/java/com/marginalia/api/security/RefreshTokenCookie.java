package com.marginalia.api.security;

/** Defines the HttpOnly cookie used to transport OAuth refresh tokens. */
public final class RefreshTokenCookie {

    public static final String NAME = "marginalia_refresh_token";

    private RefreshTokenCookie() {
    }
}
