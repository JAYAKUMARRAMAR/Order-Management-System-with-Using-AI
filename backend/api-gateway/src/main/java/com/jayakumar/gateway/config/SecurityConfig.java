package com.jayakumar.gateway.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public ReactiveJwtDecoder jwtDecoder(
                        @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {

                SecretKey secretKey = new SecretKeySpec(
                                secret.getBytes(StandardCharsets.UTF_8),
                                "HmacSHA256");

                return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
        }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http

                // Disable CSRF for REST APIs
                //.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .csrf(csrf -> csrf.disable())

                // Configure authorization
                .authorizeExchange(exchange -> exchange

                        // Public authentication APIs
                        .pathMatchers(
                                "/auth/**"
                        ).permitAll()

                        // Public product APIs
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/products/**"
                        ).permitAll()

                        // Health check
                        .pathMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // Inventory requires authentication
                        .pathMatchers(
                                "/api/inventory/**"
                        ).authenticated()

                        // Orders require authentication
                        .pathMatchers(
                                "/api/orders/**"
                        ).authenticated()

                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )

                // JWT validation
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> {})
                )

                .build();
    }
}