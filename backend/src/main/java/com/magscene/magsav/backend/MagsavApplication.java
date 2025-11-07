package com.magscene.magsav.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Application principale MAGSAV-3.0 Backend
 * Fonctionnant avec Java 21 LTS
 */
@SpringBootApplication
@EntityScan("com.magscene.magsav.backend.entity")
@EnableJpaRepositories("com.magscene.magsav.backend.repository")
public class MagsavApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MagsavApplication.class, args);
        System.out.println("\uD83D\uDE80 MAGSAV-3.0 Backend demarre avec Java " + 
                          System.getProperty("java.version"));
    }
}
