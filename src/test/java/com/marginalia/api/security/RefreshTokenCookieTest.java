package com.marginalia.api.security;

import com.marginalia.api.config.FrontendProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCookieTest {

    private static final JwtProperties JWT_PROPERTIES = new JwtProperties(
            "secret",
            Duration.ofMinutes(15),
            Duration.ofDays(7)
    );

    @Test
    void createsSecureCrossSiteCookieForProductionFrontend() {
        RefreshTokenCookie factory = new RefreshTokenCookie(
                JWT_PROPERTIES,
                new FrontendProperties("https://marginalia-front.vercel.app")
        );

        String cookie = factory.create("refresh-token").toString();

        assertThat(cookie)
                .contains("marginalia_refresh_token=refresh-token")
                .contains("Path=/api/auth")
                .contains("Max-Age=604800")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=None");
    }

    @Test
    void createsLocalCookieAndCanExpireIt() {
        RefreshTokenCookie factory = new RefreshTokenCookie(
                JWT_PROPERTIES,
                new FrontendProperties("http://localhost:5173")
        );

        String cookie = factory.clear().toString();

        assertThat(cookie)
                .contains("marginalia_refresh_token=")
                .contains("Path=/api/auth")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Lax")
                .doesNotContain("Secure");
    }
}
