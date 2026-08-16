package com.marginalia.api.security;

import com.marginalia.api.config.FrontendProperties;
import com.marginalia.api.dto.LoginResponse;
import com.marginalia.api.service.OAuthLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/** Converts a successful Google OAuth2 authentication into an SPA redirect secured by an HttpOnly refresh-token cookie. */
@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthLoginService oAuthLoginService;
    private final JwtProperties jwtProperties;
    private final FrontendProperties frontendProperties;

    /**
     * Completes an OAuth2 login, stores its refresh token in an HttpOnly cookie, and redirects to the SPA callback.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param authentication successful OAuth2 authentication
     * @throws IOException if the response cannot be written
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google account did not provide an email");
            return;
        }

        LoginResponse loginResponse = oAuthLoginService.login(email);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        ResponseCookie refreshTokenCookie = ResponseCookie.from(RefreshTokenCookie.NAME, loginResponse.refreshToken())
                .httpOnly(true)
                .secure(frontendProperties.frontendUrl().startsWith("https://"))
                .sameSite(frontendProperties.frontendUrl().startsWith("https://") ? "None" : "Lax")
                .path("/api/auth")
                .maxAge(jwtProperties.refreshTokenExpiration())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        String callbackUrl = UriComponentsBuilder.fromUriString(frontendProperties.frontendUrl())
                .path("/oauth/callback")
                .build()
                .toUriString();
        response.sendRedirect(callbackUrl);
    }
}
