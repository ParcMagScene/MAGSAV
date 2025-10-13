package com.magsav.api.controller;

import com.magsav.model.GoogleServicesConfig;
import com.magsav.repo.GoogleServicesConfigRepository;
import com.magsav.service.google.GoogleIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour la configuration des services Google dans MAGSAV 1.3
 */
@RestController
@RequestMapping("/api/v1/google/config")
@CrossOrigin(origins = "*")
public class GoogleConfigController {

    private final GoogleServicesConfigRepository configRepository;
    private final GoogleIntegrationService googleService;

    public GoogleConfigController() {
        this.configRepository = new GoogleServicesConfigRepository();
        this.googleService = GoogleIntegrationService.getInstance();
    }

    /**
     * Obtient la configuration actuelle des services Google
     */
    @GetMapping
    public ResponseEntity<GoogleServicesConfig> getGoogleConfig() {
        GoogleServicesConfig config = configRepository.findFirst();
        
        if (config == null) {
            // Créer une configuration par défaut
            config = new GoogleServicesConfig();
            config.setRedirectUri("http://localhost:8080/oauth/callback");
            config.setScopes("https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/contacts");
            config.setIntervalleSync(30);
            config.setEmailExpediteur("votre-email@gmail.com");
            config.setNomExpediteur("MAGSAV Service Client");
        }
        
        return ResponseEntity.ok(config);
    }

    /**
     * Met à jour la configuration des services Google
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateGoogleConfig(@RequestBody GoogleServicesConfig config) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean saved = configRepository.save(config);
            
            if (saved) {
                // Recharger la configuration dans le service
                googleService.reloadConfiguration();
                
                response.put("success", true);
                response.put("message", "Configuration mise à jour avec succès");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Erreur lors de la sauvegarde de la configuration");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Teste la configuration OAuth2
     */
    @PostMapping("/test-oauth")
    public ResponseEntity<Map<String, Object>> testOAuth2Config(@RequestBody GoogleServicesConfig config) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier que les champs requis sont présents
            if (config.getClientId() == null || config.getClientId().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Client ID manquant");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (config.getClientSecret() == null || config.getClientSecret().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Client Secret manquant");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Tester la configuration en créant une URL d'autorisation
            String authUrl = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=" + config.getClientId() +
                "&redirect_uri=" + config.getRedirectUri() +
                "&scope=" + config.getScopes().replace(" ", "%20") +
                "&response_type=code" +
                "&access_type=offline";
            
            response.put("success", true);
            response.put("message", "Configuration OAuth2 valide");
            response.put("auth_url", authUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du test OAuth2 : " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Génère une nouvelle configuration par défaut
     */
    @PostMapping("/reset")
    public ResponseEntity<GoogleServicesConfig> resetToDefaultConfig() {
        GoogleServicesConfig defaultConfig = new GoogleServicesConfig();
        
        // Configuration par défaut
        defaultConfig.setRedirectUri("http://localhost:8080/oauth/callback");
        defaultConfig.setScopes("https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/contacts");
        defaultConfig.setIntervalleSync(30);
        defaultConfig.setEmailExpediteur("votre-email@gmail.com");
        defaultConfig.setNomExpediteur("MAGSAV Service Client");
        defaultConfig.setSyncCalendarActif(true);
        defaultConfig.setGmailActif(true);
        defaultConfig.setContactsActif(true);
        defaultConfig.setNotificationInterventions(true);
        defaultConfig.setRappelsAutomatiques(true);
        
        return ResponseEntity.ok(defaultConfig);
    }

    /**
     * Valide la configuration complète
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConfig(@RequestBody GoogleServicesConfig config) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        // Validation OAuth2
        if (config.getClientId() == null || config.getClientId().trim().isEmpty()) {
            errors.put("clientId", "Client ID requis");
        }
        
        if (config.getClientSecret() == null || config.getClientSecret().trim().isEmpty()) {
            errors.put("clientSecret", "Client Secret requis");
        }
        
        if (config.getRedirectUri() == null || config.getRedirectUri().trim().isEmpty()) {
            errors.put("redirectUri", "Redirect URI requis");
        } else if (!config.getRedirectUri().startsWith("http")) {
            errors.put("redirectUri", "Redirect URI doit commencer par http ou https");
        }
        
        if (config.getScopes() == null || config.getScopes().trim().isEmpty()) {
            errors.put("scopes", "Scopes requis");
        }
        
        // Validation Email
        if (config.isGmailActif()) {
            if (config.getEmailExpediteur() == null || config.getEmailExpediteur().trim().isEmpty()) {
                errors.put("emailExpediteur", "Email expéditeur requis pour Gmail");
            } else if (!config.getEmailExpediteur().contains("@")) {
                errors.put("emailExpediteur", "Format d'email invalide");
            }
            
            if (config.getNomExpediteur() == null || config.getNomExpediteur().trim().isEmpty()) {
                errors.put("nomExpediteur", "Nom expéditeur requis pour Gmail");
            }
        }
        
        // Validation synchronisation
        if (config.isSyncCalendarActif()) {
            if (config.getIntervalleSync() <= 0 || config.getIntervalleSync() > 1440) {
                errors.put("intervalleSync", "Intervalle de synchronisation doit être entre 1 et 1440 minutes");
            }
        }
        
        if (errors.isEmpty()) {
            response.put("success", true);
            response.put("message", "Configuration valide");
        } else {
            response.put("success", false);
            response.put("message", "Erreurs de validation trouvées");
            response.put("errors", errors);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les statistiques d'utilisation des services Google
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGoogleServicesStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statuts des services
        stats.put("initialized", googleService.isInitialized());
        stats.put("calendar_available", googleService.isCalendarAvailable());
        stats.put("gmail_available", googleService.isGmailAvailable());
        stats.put("contacts_available", googleService.isContactsAvailable());
        
        // Configuration actuelle
        GoogleServicesConfig config = configRepository.findFirst();
        if (config != null) {
            stats.put("services_configured", config.isConfigured());
            stats.put("calendar_sync_enabled", config.isSyncCalendarActif());
            stats.put("gmail_enabled", config.isGmailActif());
            stats.put("contacts_enabled", config.isContactsActif());
            stats.put("sync_interval_minutes", config.getIntervalleSync());
        } else {
            stats.put("services_configured", false);
        }
        
        return ResponseEntity.ok(stats);
    }
}