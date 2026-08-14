package com.marginalia.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Holds validated Cloudinary account credentials.
 *
 * @param cloudName Cloudinary cloud name
 * @param apiKey Cloudinary API key
 * @param apiSecret Cloudinary API secret
 */
@Validated
@ConfigurationProperties("cloudinary")
public record CloudinaryProperties(
        @NotBlank String cloudName,
        @NotBlank String apiKey,
        @NotBlank String apiSecret
) {
}
