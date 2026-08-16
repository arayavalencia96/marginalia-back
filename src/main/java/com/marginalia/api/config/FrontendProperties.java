package com.marginalia.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Provides the SPA origin used for OAuth redirects and credentialed CORS requests. */
@Validated
@ConfigurationProperties(prefix = "app")
public record FrontendProperties(@NotBlank String frontendUrl) {
}
