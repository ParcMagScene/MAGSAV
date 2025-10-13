package com.magsav.util;

import com.magsav.service.AuthenticationService;
import com.magsav.repo.GoogleServicesConfigRepository;

/**
 * Utilitaire pour initialiser le système avec les données par défaut
 */
public class DatabaseInitializer {
    
    /**
     * Initialise la base de données avec les données essentielles
     */
    public static void initialize() {
        System.out.println("Initialisation de la base de données...");
        
        // Créer les tables Google Services
        GoogleServicesConfigRepository googleRepo = new GoogleServicesConfigRepository();
        googleRepo.createTableIfNotExists();
        
        // Créer l'administrateur par défaut
        AuthenticationService authService = new AuthenticationService();
        authService.createDefaultAdminIfNotExists();
        
        System.out.println("Initialisation terminée.");
    }
    
    public static void main(String[] args) {
        initialize();
    }
}