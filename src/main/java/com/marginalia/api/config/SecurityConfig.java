package com.marginalia.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/books/**",
                        "/api/chapters/**",
                        "/api/blocks/**"
                ))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/health",
                                "/api/books/**",
                                "/api/chapters/**",
                                "/api/blocks/**"
                        ).permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
