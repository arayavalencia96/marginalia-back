package com.marginalia.api.security;

import com.marginalia.api.config.FrontendProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Creates and clears the secure HttpOnly cookie used to transport refresh tokens. */
@Component
public final class RefreshTokenCookie {

    public static final String NAME = "marginalia_refresh_token";
    private static final String PATH = "/api/auth";

    private final JwtProperties jwtProperties;
    private final boolean secure;

    /**
     * Creates the cookie factory from JWT lifetime and frontend deployment configuration.
     *
     * @param jwtProperties token expiration configuration
     * @param frontendProperties frontend origin used to determine production cookie attributes
     */
    public RefreshTokenCookie(JwtProperties jwtProperties, FrontendProperties frontendProperties) {
        this.jwtProperties = jwtProperties;
        this.secure = frontendProperties.frontendUrl().startsWith("https://");
    }

    /**
     * Creates a refresh-token cookie suitable for the current environment.
     *
     * @param token raw refresh token
     * @return an HttpOnly response cookie
     */
    public ResponseCookie create(String token) {
        return builder(token)
                .maxAge(jwtProperties.refreshTokenExpiration())
                .build();
    }

    /**
     * Creates an expired cookie that removes the current browser session credential.
     *
     * @return an expired HttpOnly response cookie
     */
    public ResponseCookie clear() {
        return builder("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder builder(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(secure ? "None" : "Lax")
                .path(PATH);
    }
}
