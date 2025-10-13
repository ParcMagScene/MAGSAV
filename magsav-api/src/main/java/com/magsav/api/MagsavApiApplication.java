package com.magsav.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Application principale MAGSAV API 1.3
 * Point d'entrée pour l'API REST Spring Boot avec intégration Google Services
 */
@SpringBootApplication
@EnableWebMvc
@EnableAsync
@ComponentScan(basePackages = {"com.magsav.api", "com.magsav.service", "com.magsav.repo"})
@EntityScan(basePackages = {"com.magsav.api.entity", "com.magsav.model"})
public class MagsavApiApplication {

    public static void main(String[] args) {
        // Configuration système pour Google APIs
        System.setProperty("java.awt.headless", "true");
        
        // Bannière personnalisée
        System.setProperty("spring.banner.location", "classpath:banner.txt");
        
        SpringApplication app = new SpringApplication(MagsavApiApplication.class);
        
        // Profils par défaut
        app.setAdditionalProfiles("dev");
        
        app.run(args);
    }
}