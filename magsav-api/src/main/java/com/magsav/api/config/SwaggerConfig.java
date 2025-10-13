package com.magsav.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger/OpenAPI pour l'API MAGSAV 1.3
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Serveur de développement"),
                new Server().url("https://api.magsav.com").description("Serveur de production")
            ))
            .info(new Info()
                .title("MAGSAV API 1.3")
                .version("1.3.0")
                .description("API REST pour MAGSAV 1.3 - Gestion de Service Après-Vente avec intégration Google Services")
                .contact(new Contact()
                    .name("Équipe MAGSAV")
                    .email("support@magsav.com")
                    .url("https://www.magsav.com")
                )
                .license(new License()
                    .name("Propriétaire")
                    .url("https://www.magsav.com/license")
                )
                .summary("API complète pour la gestion SAV avec Google Calendar, Gmail et Contacts")
            );
    }
}