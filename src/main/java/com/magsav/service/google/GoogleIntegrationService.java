package com.magsav.service.google;

import com.magsav.model.GoogleServicesConfig;
import com.magsav.model.Planification;
import com.magsav.repo.GoogleServicesConfigRepository;
import com.magsav.util.AppLogger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service orchestrateur pour l'intégration Google
 * Coordonne Calendar, Gmail et Contacts
 */
public class GoogleIntegrationService {
    
    private static GoogleIntegrationService instance;
    
    private final GoogleServicesConfigRepository configRepository;
    private GoogleServicesConfig currentConfig;
    
    private GoogleCalendarService calendarService;
    private GoogleGmailService gmailService;
    private GoogleContactsService contactsService;
    
    private final ScheduledExecutorService scheduler;
    private boolean isInitialized = false;
    
    private GoogleIntegrationService() {
        this.configRepository = new GoogleServicesConfigRepository();
        this.scheduler = Executors.newScheduledThreadPool(2);
        loadConfiguration();
    }
    
    public static synchronized GoogleIntegrationService getInstance() {
        if (instance == null) {
            instance = new GoogleIntegrationService();
        }
        return instance;
    }
    
    /**
     * Initialise tous les services Google
     */
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AppLogger.info("google", "Initialisation de l'intégration Google");
                
                if (currentConfig == null || !currentConfig.isConfigured()) {
                    AppLogger.warn("google", "Configuration Google manquante ou incomplète");
                    return false;
                }
                
                // Initialiser les services
                calendarService = new GoogleCalendarService(currentConfig);
                gmailService = new GoogleGmailService(currentConfig);
                contactsService = new GoogleContactsService(currentConfig);
                
                // Tester les connexions
                boolean calendarOK = calendarService.initialize().get();
                boolean gmailOK = gmailService.testConnection().get();
                boolean contactsOK = contactsService.testConnection().get();
                
                isInitialized = calendarOK || gmailOK || contactsOK;
                
                if (isInitialized) {
                    // Démarrer la synchronisation automatique
                    startAutoSync();
                    AppLogger.info("google", "Intégration Google initialisée avec succès");
                } else {
                    AppLogger.warn("google", "Aucun service Google n'a pu être initialisé");
                }
                
                return isInitialized;
                
            } catch (Exception e) {
                AppLogger.error("Erreur initialisation intégration Google: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Synchronise une planification avec Google Calendar
     */
    public CompletableFuture<Boolean> syncPlanification(Planification planification) {
        if (!isInitialized || calendarService == null || !currentConfig.isSyncCalendarActif()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                AppLogger.info("google", "Synchronisation planification #" + planification.getId() + " avec Google Calendar");
                
                if (planification.getGoogleEventId() == null || planification.getGoogleEventId().isEmpty()) {
                    // Créer un nouvel événement
                    String eventId = calendarService.createEvent(planification).get();
                    return eventId != null && !eventId.isEmpty();
                } else {
                    // Mettre à jour l'événement existant
                    return calendarService.updateEvent(planification).get();
                }
                
            } catch (Exception e) {
                AppLogger.error("Erreur synchronisation planification: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Supprime une planification de Google Calendar
     */
    public CompletableFuture<Boolean> deletePlanification(String googleEventId) {
        if (!isInitialized || calendarService == null || !currentConfig.isSyncCalendarActif()) {
            return CompletableFuture.completedFuture(false);
        }
        
        if (googleEventId == null || googleEventId.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                AppLogger.info("google", "Suppression événement Google Calendar: " + googleEventId);
                return calendarService.deleteEvent(googleEventId).get();
            } catch (Exception e) {
                AppLogger.error("Erreur suppression événement Google Calendar: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Envoie une notification email pour une intervention
     */
    public CompletableFuture<Boolean> sendInterventionNotification(String clientEmail, String clientNom, 
                                                                   String technicienNom, String dateIntervention, 
                                                                   String typeIntervention) {
        if (!isInitialized || gmailService == null || !currentConfig.isGmailActif()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return gmailService.sendInterventionNotification(clientEmail, clientNom, technicienNom, 
                                                         dateIntervention, typeIntervention);
    }
    
    /**
     * Envoie un rappel d'intervention
     */
    public CompletableFuture<Boolean> sendInterventionReminder(String clientEmail, String clientNom, 
                                                               String technicienNom, String dateIntervention, 
                                                               String heureIntervention) {
        if (!isInitialized || gmailService == null || !currentConfig.isGmailActif()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return gmailService.sendInterventionReminder(clientEmail, clientNom, technicienNom, 
                                                     dateIntervention, heureIntervention);
    }
    
    /**
     * Envoie une confirmation de commande
     */
    public CompletableFuture<Boolean> sendOrderConfirmation(String fournisseurEmail, String fournisseurNom,
                                                            String numeroCommande, String montantTotal,
                                                            String dateCommande) {
        if (!isInitialized || gmailService == null || !currentConfig.isGmailActif()) {
            return CompletableFuture.completedFuture(false);
        }
        
        String detailsCommande = "Date: " + dateCommande + " - Fournisseur: " + fournisseurNom;
        return gmailService.sendOrderConfirmation(fournisseurEmail, numeroCommande, 
                                                  montantTotal, detailsCommande);
    }
    
    /**
     * Synchronise les contacts avec Google
     */
    public CompletableFuture<List<Map<String, Object>>> syncContacts() {
        if (!isInitialized || contactsService == null || !currentConfig.isContactsActif()) {
            return CompletableFuture.completedFuture(List.of());
        }
        
        return contactsService.syncContactsFromGoogle();
    }
    
    /**
     * Ajoute un client aux contacts Google
     */
    public CompletableFuture<String> addClientToGoogleContacts(String nom, String email, 
                                                               String telephone, String entreprise) {
        if (!isInitialized || contactsService == null || !currentConfig.isContactsActif()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return contactsService.createContact(nom, email, telephone, entreprise);
    }
    
    /**
     * Démarre la synchronisation automatique
     */
    public void startAutoSync() {
        if (!isInitialized || currentConfig == null) {
            return;
        }
        
        int intervalleMinutes = currentConfig.getIntervalleSync();
        
        AppLogger.info("google", "Démarrage synchronisation automatique (intervalle: " + intervalleMinutes + " min)");
        
        // Synchronisation Calendar
        if (currentConfig.isSyncCalendarActif()) {
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    AppLogger.info("google", "Synchronisation automatique Calendar");
                    calendarService.syncFromGoogle().get();
                } catch (Exception e) {
                    AppLogger.error("Erreur sync auto Calendar: " + e.getMessage());
                }
            }, intervalleMinutes, intervalleMinutes, TimeUnit.MINUTES);
        }
        
        // Synchronisation Contacts
        if (currentConfig.isSyncContactsAuto()) {
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    AppLogger.info("google", "Synchronisation automatique Contacts");
                    contactsService.syncContactsFromGoogle().get();
                } catch (Exception e) {
                    AppLogger.error("Erreur sync auto Contacts: " + e.getMessage());
                }
            }, intervalleMinutes * 2, intervalleMinutes * 2, TimeUnit.MINUTES); // Moins fréquent
        }
    }
    
    /**
     * Arrête la synchronisation automatique
     */
    public void stopAutoSync() {
        AppLogger.info("google", "Arrêt synchronisation automatique");
        scheduler.shutdownNow();
    }
    
    /**
     * Recharge la configuration
     */
    public boolean reloadConfiguration() {
        try {
            loadConfiguration();
            
            if (isInitialized) {
                stopAutoSync();
                return initialize().get();
            }
            
            return true;
            
        } catch (Exception e) {
            AppLogger.error("Erreur rechargement configuration Google: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtient l'URL d'autorisation OAuth2
     */
    public String getAuthorizationUrl() {
        if (currentConfig == null || !currentConfig.isConfigured()) {
            throw new IllegalStateException("Configuration Google non complète");
        }
        
        GoogleAuthService authService = new GoogleAuthService(currentConfig);
        return authService.getAuthorizationUrl();
    }
    
    /**
     * Échange le code d'autorisation contre des tokens
     */
    public CompletableFuture<Boolean> exchangeAuthorizationCode(String code) {
        if (currentConfig == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                GoogleAuthService authService = new GoogleAuthService(currentConfig);
                boolean success = authService.exchangeCodeForTokens(code);
                
                if (success) {
                    // Sauvegarder la configuration mise à jour
                    configRepository.save(currentConfig);
                    AppLogger.info("google", "Tokens d'authentification sauvegardés");
                }
                
                return success;
                
            } catch (Exception e) {
                AppLogger.error("Erreur échange code autorisation: " + e.getMessage());
                return false;
            }
        });
    }
    
    // Méthodes privées
    
    private void loadConfiguration() {
        try {
            List<GoogleServicesConfig> configs = configRepository.findAll();
            
            if (configs.isEmpty()) {
                AppLogger.info("google", "Création configuration Google par défaut");
                currentConfig = new GoogleServicesConfig("Configuration par défaut");
                configRepository.save(currentConfig);
            } else {
                currentConfig = configs.stream()
                        .filter(GoogleServicesConfig::isActif)
                        .findFirst()
                        .orElse(configs.get(0));
                
                AppLogger.info("google", "Configuration chargée: " + currentConfig.getNom());
            }
            
        } catch (Exception e) {
            AppLogger.error("Erreur chargement configuration Google: " + e.getMessage());
            currentConfig = new GoogleServicesConfig("Configuration par défaut");
        }
    }
    
    // Getters et statut
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public GoogleServicesConfig getCurrentConfig() {
        return currentConfig;
    }
    
    public boolean isCalendarAvailable() {
        return isInitialized && calendarService != null && currentConfig.isSyncCalendarActif();
    }
    
    public boolean isGmailAvailable() {
        return isInitialized && gmailService != null && currentConfig.isGmailActif();
    }
    
    public boolean isContactsAvailable() {
        return isInitialized && contactsService != null && currentConfig.isContactsActif();
    }
    
    public void shutdown() {
        AppLogger.info("google", "Arrêt de l'intégration Google");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}