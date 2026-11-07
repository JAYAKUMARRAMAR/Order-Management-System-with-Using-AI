package com.jayakumar.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${internal.api.token:}") String internalApiToken) throws Exception {

        http
            // REST API என்பதால் CSRF disable
            .csrf(csrf -> csrf.disable())

            // Basic Auth / Form Login disable
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // Internal service endpoints require the service credential.
                .requestMatchers("/internal/**").hasRole("SERVICE")

                // Authentication APIs public
                .requestMatchers("/auth/register", "/auth/login", "/auth/verify-email").permitAll()

                // Health check public
                .requestMatchers("/actuator/health").permitAll()

                // Other APIs
                .anyRequest().authenticated()
            );

        http.addFilterBefore(
                new InternalServiceAuthenticationFilter(internalApiToken),
                AnonymousAuthenticationFilter.class);

        return http.build();
    }
}