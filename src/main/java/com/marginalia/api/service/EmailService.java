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

/** Sends transactional verification-code emails through the Brevo REST API. */
@Service
public class EmailService {

    private static final String VERIFICATION_SUBJECT = "Verify your Marginalia account";
    private static final String PASSWORD_RESET_SUBJECT = "Reset your Marginalia password";
    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final RestClient brevoRestClient;
    private final BrevoProperties properties;
    private final String verificationTemplate;

    /**
     * Creates the service and loads the HTML verification template from application resources.
     *
     * @param brevoRestClient configured Brevo REST client
     * @param properties Brevo sender configuration
     * @param verificationTemplate HTML template resource
     * @throws IOException if the email template cannot be read
     */
    public EmailService(
            RestClient brevoRestClient,
            BrevoProperties properties,
            @Value("classpath:templates/verification-code.html") Resource verificationTemplate
    ) throws IOException {
        this.brevoRestClient = brevoRestClient;
        this.properties = properties;
        this.verificationTemplate = verificationTemplate.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Sends a verification code to an email address using the configured HTML template.
     *
     * @param email recipient email address
     * @param code verification code to insert into the template
     * @throws EmailDeliveryException if Brevo rejects the request or cannot be reached
     */
    public void sendVerificationCode(String email, String code) {
        send(email, VERIFICATION_SUBJECT, verificationTemplate.replace(CODE_PLACEHOLDER, code));
    }

    /**
     * Sends a time-limited password reset link.
     *
     * @param email recipient email address
     * @param resetUrl one-time URL that opens the reset form
     * @throws EmailDeliveryException if Brevo rejects the request or cannot be reached
     */
    public void sendPasswordResetLink(String email, String resetUrl) {
        String html = "<p>We received a request to reset your Marginalia password.</p>"
                + "<p><a href=\"" + resetUrl + "\">Reset password</a></p>"
                + "<p>This link expires in 15 minutes. If you did not request this, you can ignore this email.</p>";
        send(email, PASSWORD_RESET_SUBJECT, html);
    }

    private void send(String email, String subject, String htmlContent) {
        Sender sender = new Sender(properties.senderEmail(), properties.senderName());
        Recipient recipient = new Recipient(email);
        SendEmailRequest request = new SendEmailRequest(sender, List.of(recipient), subject, htmlContent);

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
