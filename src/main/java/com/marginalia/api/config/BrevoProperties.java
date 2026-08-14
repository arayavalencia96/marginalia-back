package com.marginalia.api.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

/**
 * Holds validated Brevo API and sender configuration.
 *
 * @param baseUrl Brevo API base URL
 * @param apiKey Brevo API authentication key
 * @param senderEmail verified sender email address
 * @param senderName display name used for outgoing email
 */
@Validated
@ConfigurationProperties("brevo")
public record BrevoProperties(
        @NotNull URI baseUrl,
        @NotBlank String apiKey,
        @NotBlank @Email String senderEmail,
        @NotBlank String senderName
) {
}
