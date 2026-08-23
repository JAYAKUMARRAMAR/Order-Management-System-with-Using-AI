package com.jayakumar.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            // REST API என்பதால் CSRF disable
            .csrf(csrf -> csrf.disable())

            // Basic Auth / Form Login disable
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // Login API public
                .requestMatchers("/auth/login").permitAll()

                // Health check public
                .requestMatchers("/actuator/health").permitAll()

                // Other APIs
                .anyRequest().authenticated()
            );

        return http.build();
    }
}