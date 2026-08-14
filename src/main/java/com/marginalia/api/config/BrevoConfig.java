package com.marginalia.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Configures the authenticated REST client used to call Brevo's transactional email API. */
@Configuration
@EnableConfigurationProperties(BrevoProperties.class)
public class BrevoConfig {

    @Bean
    RestClient brevoRestClient(RestClient.Builder builder, BrevoProperties properties) {
        return builder
                .baseUrl(properties.baseUrl().toString())
                .defaultHeader("api-key", properties.apiKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
