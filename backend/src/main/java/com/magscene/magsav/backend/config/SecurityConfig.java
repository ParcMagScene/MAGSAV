package com.magscene.magsav.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration de sécurité pour MAGSAV-3.0
 * 
 * - Profil "development" : Configuration permissive pour développement/tests
 * - Profil "production" : Configuration sécurisée pour production
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.profiles.active:development}")
    private String activeProfile;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        if ("production".equals(activeProfile)) {
            // Configuration sécurisée pour production
            http.authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/health")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).authenticated()
                .anyRequest().denyAll()
            );
        } else {
            // Configuration permissive pour développement
            http.authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/health")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).permitAll()
                .anyRequest().permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Pour H2 Console
            );
        }
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        if ("production".equals(activeProfile)) {
            // CORS strict en production
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
            configuration.setAllowCredentials(true);
        } else {
            // CORS permissif en développement
            configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "*"));
            configuration.setAllowCredentials(false);
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Content-Length", "X-Requested-With", "Location", "Authorization"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
