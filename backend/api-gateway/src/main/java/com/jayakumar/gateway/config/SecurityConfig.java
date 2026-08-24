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

                   .cors(cors -> {})

    .authorizeExchange(exchange -> exchange

        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        .pathMatchers(
            "/auth/**"
        ).permitAll()

        .pathMatchers(
            HttpMethod.GET,
            "/api/products/**"
        ).permitAll()

        .pathMatchers(
            "/actuator/health"
        ).permitAll()

        .pathMatchers(
            "/api/inventory/**"
        ).authenticated()

        .pathMatchers(
            "/api/orders/**"
        ).authenticated()

        .anyExchange().authenticated()
    )

    .oauth2ResourceServer(
        oauth2 -> oauth2.jwt(jwt -> {})
    )

    .build();

    }
}