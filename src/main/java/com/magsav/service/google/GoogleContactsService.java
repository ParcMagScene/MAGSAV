package com.magsav.service.google;

import com.magsav.model.GoogleServicesConfig;
import com.magsav.util.AppLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service d'intégration avec Google Contacts API
 * Gère la synchronisation des contacts clients
 */
public class GoogleContactsService {
    
    private static final String CONTACTS_API_URL = "https://people.googleapis.com/v1";
    
    private final GoogleAuthService authService;
    private final GoogleServicesConfig config;
    private final HttpClient httpClient;
    
    public GoogleContactsService(GoogleServicesConfig config) {
        this.config = config;
        this.authService = new GoogleAuthService(config);
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * Synchronise les contacts depuis Google
     */
    public CompletableFuture<List<Map<String, Object>>> syncContactsFromGoogle() {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                throw new RuntimeException("Service Google Contacts non authentifié");
            }
            
            if (!config.isContactsActif()) {
                AppLogger.warn("contacts", "Service Google Contacts désactivé");
                return List.of();
            }
            
            try {
                AppLogger.info("contacts", "Synchronisation des contacts depuis Google");
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(CONTACTS_API_URL + "/people/me/connections?personFields=names,emailAddresses,phoneNumbers,organizations"))
                        .header("Authorization", "Bearer " + authService.getValidAccessToken())
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    List<Map<String, Object>> contacts = parseContactsResponse(response.body());
                    AppLogger.info("contacts", "Synchronisation terminée: " + contacts.size() + " contacts");
                    return contacts;
                } else {
                    AppLogger.error("Erreur synchronisation contacts: " + response.statusCode());
                    throw new RuntimeException("Erreur API Google Contacts: " + response.statusCode());
                }
                
            } catch (Exception e) {
                AppLogger.error("Erreur synchronisation contacts: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Crée un contact dans Google Contacts
     */
    public CompletableFuture<String> createContact(String nom, String email, String telephone, String entreprise) {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                throw new RuntimeException("Service Google Contacts non authentifié");
            }
            
            try {
                AppLogger.info("contacts", "Création contact Google: " + nom);
                
                String contactJson = buildContactJson(nom, email, telephone, entreprise);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(CONTACTS_API_URL + "/people:createContact"))
                        .header("Authorization", "Bearer " + authService.getValidAccessToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(contactJson))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    String contactId = extractContactId(response.body());
                    AppLogger.info("contacts", "Contact créé avec ID: " + contactId);
                    return contactId;
                } else {
                    AppLogger.error("Erreur création contact: " + response.statusCode());
                    throw new RuntimeException("Erreur création contact Google: " + response.statusCode());
                }
                
            } catch (Exception e) {
                AppLogger.error("Erreur création contact Google: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Met à jour un contact existant
     */
    public CompletableFuture<Boolean> updateContact(String contactId, String nom, String email, 
                                                    String telephone, String entreprise) {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                return false;
            }
            
            try {
                AppLogger.info("contacts", "Mise à jour contact Google ID: " + contactId);
                
                String contactJson = buildContactJson(nom, email, telephone, entreprise);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(CONTACTS_API_URL + "/people/" + contactId + ":updateContact?updatePersonFields=names,emailAddresses,phoneNumbers,organizations"))
                        .header("Authorization", "Bearer " + authService.getValidAccessToken())
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(contactJson))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                boolean success = response.statusCode() == 200;
                AppLogger.info("contacts", "Contact mis à jour: " + (success ? "OK" : "ECHEC"));
                
                return success;
                
            } catch (Exception e) {
                AppLogger.error("Erreur mise à jour contact Google: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Recherche un contact par email
     */
    public CompletableFuture<Map<String, Object>> findContactByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                return null;
            }
            
            try {
                AppLogger.info("contacts", "Recherche contact par email: " + email);
                
                // Note: L'API Google Contacts ne permet pas de recherche directe par email
                // Il faut récupérer tous les contacts et filtrer localement
                List<Map<String, Object>> allContacts = syncContactsFromGoogle().get();
                
                return allContacts.stream()
                        .filter(contact -> {
                            @SuppressWarnings("unchecked")
                            List<String> emails = (List<String>) contact.get("emails");
                            return emails != null && emails.contains(email);
                        })
                        .findFirst()
                        .orElse(null);
                
            } catch (Exception e) {
                AppLogger.error("Erreur recherche contact: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Teste la connexion aux contacts Google
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                return false;
            }
            
            try {
                AppLogger.info("contacts", "Test de connexion Google Contacts API");
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(CONTACTS_API_URL + "/people/me?personFields=names"))
                        .header("Authorization", "Bearer " + authService.getValidAccessToken())
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                boolean success = response.statusCode() == 200;
                AppLogger.info("contacts", "Test connexion Google Contacts: " + (success ? "OK" : "ECHEC"));
                
                return success;
                
            } catch (Exception e) {
                AppLogger.error("Erreur test connexion Google Contacts: " + e.getMessage());
                return false;
            }
        });
    }
    
    // Méthodes privées
    
    private String buildContactJson(String nom, String email, String telephone, String entreprise) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Nom
        if (nom != null && !nom.isEmpty()) {
            json.append("\"names\":[{\"displayName\":\"").append(nom).append("\"}],");
        }
        
        // Email
        if (email != null && !email.isEmpty()) {
            json.append("\"emailAddresses\":[{\"value\":\"").append(email).append("\"}],");
        }
        
        // Téléphone
        if (telephone != null && !telephone.isEmpty()) {
            json.append("\"phoneNumbers\":[{\"value\":\"").append(telephone).append("\"}],");
        }
        
        // Entreprise
        if (entreprise != null && !entreprise.isEmpty()) {
            json.append("\"organizations\":[{\"name\":\"").append(entreprise).append("\"}],");
        }
        
        // Retirer la dernière virgule
        if (json.length() > 1 && json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }
        
        json.append("}");
        return json.toString();
    }
    
    private List<Map<String, Object>> parseContactsResponse(String response) {
        // TODO: Parser la vraie réponse JSON de Google Contacts
        AppLogger.info("contacts", "Parsing réponse contacts: " + response.substring(0, Math.min(100, response.length())));
        
        // Simulation pour l'instant
        return List.of(
            Map.of(
                "id", "people/contact123",
                "name", "Contact Test",
                "emails", List.of("test@example.com"),
                "phones", List.of("0123456789"),
                "organization", "Test Company"
            )
        );
    }
    
    private String extractContactId(String response) {
        // TODO: Extraire l'ID du contact depuis la réponse JSON
        AppLogger.info("contacts", "Extraction ID contact depuis: " + response.substring(0, Math.min(50, response.length())));
        
        // Simulation
        return "people/contact_" + System.currentTimeMillis();
    }
    
    // Getters
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }
    
    public GoogleServicesConfig getConfig() {
        return config;
    }
}