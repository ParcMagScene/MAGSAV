package com.magsav.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Application web Spring Boot pour MAGSAV
 * Interface graphique moderne accessible via navigateur web
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.magsav"})
public class MAGSAVWebApp {
    
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de l'interface web MAGSAV...");
        System.out.println("📱 Accès via navigateur : http://localhost:8080");
        
        SpringApplication.run(MAGSAVWebApp.class, args);
    }
}