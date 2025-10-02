package com.magsav.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    // Autoriser les forwards internes et la page d'erreur
                    .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR)
                    .permitAll()
                    // Pages publiques
                    .requestMatchers("/", "/index", "/login")
                    .permitAll()
                    // Ressources statiques (CSS, JS, images) et assets
                    .requestMatchers("/webjars/**", "/assets/**", "/style/**", "/templates/**")
                    .permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**")
                    .permitAll()
                    // Photos produits: lecture publique, écriture admin
                    .requestMatchers(HttpMethod.GET, "/product/*/photo")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/product/*/photo")
                    .hasRole("ADMIN")
                    // API publiques minimales
                    .requestMatchers(HttpMethod.GET, "/api/dossiers")
                    .permitAll()
                    // Détails intervention accessibles aux utilisateurs authentifiés (routes legacy
                    // et nouvelles)
                    .requestMatchers(HttpMethod.GET, "/intervention/*", "/dossier/*")
                    .hasAnyRole("USER", "ADMIN")
                    // Écriture intervention réservée admin (routes legacy et nouvelles)
                    .requestMatchers(HttpMethod.POST, "/intervention/**", "/dossier/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/intervention/**", "/dossier/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/intervention/**", "/dossier/**")
                    .hasRole("ADMIN")
                    // Administration catégories
                    .requestMatchers("/categories/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    // Le reste nécessite authentication
                    .anyRequest()
                    .authenticated())
        .headers(
            headers ->
                headers
                    // Politique CSP par défaut stricte, compatible avec nos besoins actuels
                    // (images/typos en data:, styles inline pour Thymeleaf)
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data: blob:; style-src 'self'; script-src 'self'; font-src 'self' data:; object-src 'none'; base-uri 'self'; frame-ancestors 'none'"))
                    // Politique de référent moderne
                    .referrerPolicy(
                        ref ->
                            ref.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy
                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    // Désactiver explicitement certaines capacités du navigateur
                    .permissionsPolicy(pp -> pp.policy("camera=(), microphone=(), geolocation=()")))
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll());

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user =
        User.builder()
            .username("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build();

    UserDetails admin =
        User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN", "USER")
            .build();

    return new InMemoryUserDetailsManager(user, admin);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
