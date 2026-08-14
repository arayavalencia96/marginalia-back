package com.marginalia.api.service;

import com.marginalia.api.config.BrevoProperties;
import com.marginalia.api.exception.EmailDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailService {

    private static final String VERIFICATION_SUBJECT = "Verify your Marginalia account";
    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final RestClient brevoRestClient;
    private final BrevoProperties properties;
    private final String verificationTemplate;

    public EmailService(
            RestClient brevoRestClient,
            BrevoProperties properties,
            @Value("classpath:templates/verification-code.html") Resource verificationTemplate
    ) throws IOException {
        this.brevoRestClient = brevoRestClient;
        this.properties = properties;
        this.verificationTemplate = verificationTemplate.getContentAsString(StandardCharsets.UTF_8);
    }

    public void sendVerificationCode(String email, String code) {
        Sender sender = new Sender(properties.senderEmail(), properties.senderName());
        Recipient recipient = new Recipient(email);
        SendEmailRequest request = new SendEmailRequest(
                sender,
                List.of(recipient),
                VERIFICATION_SUBJECT,
                verificationTemplate.replace(CODE_PLACEHOLDER, code)
        );

        try {
            brevoRestClient.post()
                    .uri("/v3/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new EmailDeliveryException(exception);
        }
    }

    private record Sender(String email, String name) {
    }

    private record Recipient(String email) {
    }

    private record SendEmailRequest(
            Sender sender,
            List<Recipient> to,
            String subject,
            String htmlContent
    ) {
    }
}
