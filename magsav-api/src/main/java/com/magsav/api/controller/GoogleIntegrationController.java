package com.magsav.api.controller;

import com.magsav.service.google.GoogleIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Contrôleur REST pour l'intégration Google Services dans MAGSAV 1.3
 */
@RestController
@RequestMapping("/api/v1/google")
@CrossOrigin(origins = "*")
public class GoogleIntegrationController {

    private final GoogleIntegrationService googleService;

    public GoogleIntegrationController() {
        this.googleService = GoogleIntegrationService.getInstance();
    }

    /**
     * Obtient le statut des services Google
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGoogleServicesStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("initialized", googleService.isInitialized());
        status.put("calendar_available", googleService.isCalendarAvailable());
        status.put("gmail_available", googleService.isGmailAvailable());
        status.put("contacts_available", googleService.isContactsAvailable());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Initialise les services Google
     */
    @PostMapping("/initialize")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> initializeGoogleServices() {
        return googleService.initialize().thenApply(success -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Services Google initialisés" : "Échec de l'initialisation");
            
            if (success) {
                response.put("calendar_available", googleService.isCalendarAvailable());
                response.put("gmail_available", googleService.isGmailAvailable());
                response.put("contacts_available", googleService.isContactsAvailable());
            }
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * Synchronise une planification avec Google Calendar
     */
    @PostMapping("/calendar/sync-planification")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> syncPlanification(
            @RequestBody Map<String, Object> planificationData) {
        
        // TODO: Convertir les données en objet Planification
        // Pour l'instant, retourner une réponse simulée
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Planification synchronisée avec Google Calendar");
        response.put("event_id", "simulated_event_id_" + System.currentTimeMillis());
        
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    /**
     * Envoie une notification d'intervention
     */
    @PostMapping("/gmail/send-intervention-notification")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendInterventionNotification(
            @RequestBody Map<String, String> notificationData) {
        
        String clientEmail = notificationData.get("clientEmail");
        String clientNom = notificationData.get("clientNom");
        String technicienNom = notificationData.get("technicienNom");
        String dateIntervention = notificationData.get("dateIntervention");
        String typeIntervention = notificationData.get("typeIntervention");
        
        return googleService.sendInterventionNotification(
            clientEmail, clientNom, technicienNom, dateIntervention, typeIntervention
        ).thenApply(success -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? 
                "Notification d'intervention envoyée" : 
                "Échec de l'envoi de la notification");
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * Envoie un rappel d'intervention  
     */
    @PostMapping("/gmail/send-intervention-reminder")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendInterventionReminder(
            @RequestBody Map<String, String> reminderData) {
        
        String clientEmail = reminderData.get("clientEmail");
        String clientNom = reminderData.get("clientNom");
        String technicienNom = reminderData.get("technicienNom");
        String dateIntervention = reminderData.get("dateIntervention");
        String heureIntervention = reminderData.get("heureIntervention");
        
        return googleService.sendInterventionReminder(
            clientEmail, clientNom, technicienNom, dateIntervention, heureIntervention
        ).thenApply(success -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? 
                "Rappel d'intervention envoyé" : 
                "Échec de l'envoi du rappel");
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * Envoie une confirmation de commande
     */
    @PostMapping("/gmail/send-order-confirmation")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendOrderConfirmation(
            @RequestBody Map<String, String> orderData) {
        
        String fournisseurEmail = orderData.get("fournisseurEmail");
        String fournisseurNom = orderData.get("fournisseurNom");
        String numeroCommande = orderData.get("numeroCommande");
        String montantTotal = orderData.get("montantTotal");
        String dateCommande = orderData.get("dateCommande");
        
        return googleService.sendOrderConfirmation(
            fournisseurEmail, fournisseurNom, numeroCommande, montantTotal, dateCommande
        ).thenApply(success -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? 
                "Confirmation de commande envoyée" : 
                "Échec de l'envoi de la confirmation");
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * Synchronise les contacts avec Google
     */
    @PostMapping("/contacts/sync")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> syncContacts() {
        return googleService.syncContacts().thenApply(contacts -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts_count", contacts.size());
            response.put("contacts", contacts);
            response.put("message", contacts.size() + " contacts synchronisés");
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * Ajoute un client aux contacts Google
     */
    @PostMapping("/contacts/add-client")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> addClientToGoogleContacts(
            @RequestBody Map<String, String> clientData) {
        
        String nom = clientData.get("nom");
        String email = clientData.get("email");
        String telephone = clientData.get("telephone");
        String entreprise = clientData.get("entreprise");
        
        return googleService.addClientToGoogleContacts(nom, email, telephone, entreprise)
            .thenApply(contactId -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", contactId != null);
                response.put("contact_id", contactId);
                response.put("message", contactId != null ? 
                    "Client ajouté aux contacts Google" : 
                    "Échec de l'ajout du client");
                
                return ResponseEntity.ok(response);
            });
    }

    /**
     * Recharge la configuration Google
     */
    @PostMapping("/config/reload")
    public ResponseEntity<Map<String, Object>> reloadConfiguration() {
        boolean success = googleService.reloadConfiguration();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? 
            "Configuration rechargée" : 
            "Échec du rechargement de la configuration");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Démarre la synchronisation automatique
     */
    @PostMapping("/sync/start-auto")
    public ResponseEntity<Map<String, Object>> startAutoSync() {
        if (!googleService.isInitialized()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Services Google non initialisés");
            return ResponseEntity.badRequest().body(response);
        }
        
        googleService.startAutoSync();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Synchronisation automatique démarrée");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test de connectivité pour tous les services Google
     */
    @GetMapping("/test-connection")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> testGoogleConnections() {
        return googleService.initialize().thenApply(initialized -> {
            Map<String, Object> response = new HashMap<>();
            
            if (!initialized) {
                response.put("success", false);
                response.put("message", "Services Google non initialisés");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Boolean> tests = new HashMap<>();
            tests.put("calendar", googleService.isCalendarAvailable());
            tests.put("gmail", googleService.isGmailAvailable());
            tests.put("contacts", googleService.isContactsAvailable());
            
            response.put("success", true);
            response.put("tests", tests);
            response.put("message", "Tests de connectivité terminés");
            
            return ResponseEntity.ok(response);
        });
    }
}