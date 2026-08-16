package com.marginalia.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the generated OpenAPI contract and JWT bearer authentication scheme. */
@Configuration
public class OpenApiConfig {

    static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    OpenAPI marginaliaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marginalia API")
                        .description("REST API for structured book annotations")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
