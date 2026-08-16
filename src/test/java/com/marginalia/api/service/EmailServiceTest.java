package com.marginalia.api.service;

import com.marginalia.api.config.BrevoProperties;
import com.marginalia.api.exception.EmailDeliveryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec request;

    @Mock
    private RestClient.ResponseSpec response;

    @Mock
    private Resource template;

    private EmailService service;

    @BeforeEach
    void setUp() throws IOException {
        when(template.getContentAsString(StandardCharsets.UTF_8)).thenReturn("Code: {{verificationCode}}");
        service = new EmailService(
                restClient,
                new BrevoProperties(URI.create("https://api.brevo.com"), "key", "sender@example.com", "Marginalia"),
                template
        );
    }

    @Test
    void sendsRenderedVerificationEmailThroughBrevo() {
        when(restClient.post()).thenReturn(request);
        when(request.uri("/v3/smtp/email")).thenReturn(request);
        when(request.contentType(MediaType.APPLICATION_JSON)).thenReturn(request);
        when(request.body(org.mockito.ArgumentMatchers.any(Object.class))).thenReturn(request);
        when(request.retrieve()).thenReturn(response);
        when(response.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        service.sendVerificationCode("user@example.com", "123456");

        verify(request).body(argThat((Object body) -> body.toString().contains("123456")));
        verify(response).toBodilessEntity();
    }

    @Test
    void wrapsBrevoClientFailure() {
        when(restClient.post()).thenReturn(request);
        when(request.uri("/v3/smtp/email")).thenReturn(request);
        when(request.contentType(MediaType.APPLICATION_JSON)).thenReturn(request);
        when(request.body(org.mockito.ArgumentMatchers.any(Object.class))).thenReturn(request);
        when(request.retrieve()).thenThrow(new RestClientException("failure"));

        assertThatThrownBy(() -> service.sendVerificationCode("user@example.com", "123456"))
                .isInstanceOf(EmailDeliveryException.class);
    }

    @Test
    void sendsPasswordResetLinkThroughBrevo() {
        when(restClient.post()).thenReturn(request);
        when(request.uri("/v3/smtp/email")).thenReturn(request);
        when(request.contentType(MediaType.APPLICATION_JSON)).thenReturn(request);
        when(request.body(org.mockito.ArgumentMatchers.any(Object.class))).thenReturn(request);
        when(request.retrieve()).thenReturn(response);
        when(response.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        service.sendPasswordResetLink("user@example.com", "https://app.example/reset-password?token=token");

        verify(request).body(argThat((Object body) -> body.toString().contains("reset-password?token=token")));
    }
}
