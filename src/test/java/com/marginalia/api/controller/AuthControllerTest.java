package com.marginalia.api.controller;

import com.marginalia.api.dto.LoginRequest;
import com.marginalia.api.dto.LoginResponse;
import com.marginalia.api.dto.RefreshResponse;
import com.marginalia.api.exception.InvalidRefreshTokenException;
import com.marginalia.api.security.RefreshTokenCookie;
import com.marginalia.api.service.AuthService;
import com.marginalia.api.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private RefreshTokenCookie refreshTokenCookie;

    @InjectMocks
    private AuthController controller;

    @Test
    void loginReturnsOnlyAccessTokenAndSetsRefreshCookie() {
        LoginRequest request = new LoginRequest("reader@example.com", "password123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseCookie cookie = ResponseCookie.from(RefreshTokenCookie.NAME, "refresh").build();
        when(authService.login(request)).thenReturn(new LoginResponse("access", "refresh"));
        when(refreshTokenCookie.create("refresh")).thenReturn(cookie);

        RefreshResponse result = controller.login(request, response);

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isEqualTo(cookie.toString());
    }

    @Test
    void refreshAcceptsOnlyCookieCredential() {
        when(authService.refresh("refresh")).thenReturn(new RefreshResponse("access"));

        assertThat(controller.refresh("refresh").accessToken()).isEqualTo("access");
        verify(authService).refresh("refresh");
        assertThatThrownBy(() -> controller.refresh(null)).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logoutRevokesCredentialAndExpiresCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseCookie expiredCookie = ResponseCookie.from(RefreshTokenCookie.NAME, "").maxAge(0).build();
        when(refreshTokenCookie.clear()).thenReturn(expiredCookie);

        controller.logout("refresh", response);

        verify(authService).logout("refresh");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("marginalia_refresh_token=")
                .contains("Max-Age=0");
    }

    @Test
    void logoutWithoutCookieRemainsIdempotent() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(refreshTokenCookie.clear()).thenReturn(ResponseCookie.from(RefreshTokenCookie.NAME, "").build());

        controller.logout(null, response);

        verifyNoInteractions(authService);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNotBlank();
    }
}
