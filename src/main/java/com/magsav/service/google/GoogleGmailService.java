package com.magsav.service.google;

import com.magsav.model.GoogleServicesConfig;
import com.magsav.util.AppLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Service d'intégration avec Gmail API
 * Gère l'envoi d'emails et la gestion des modèles
 */
public class GoogleGmailService {
    
    private static final String GMAIL_API_URL = "https://gmail.googleapis.com/gmail/v1";
    
    private final GoogleAuthService authService;
    private final GoogleServicesConfig config;
    private final HttpClient httpClient;
    
    public GoogleGmailService(GoogleServicesConfig config) {
        this.config = config;
        this.authService = new GoogleAuthService(config);
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * Envoie un email via Gmail API
     */
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String body) {
        return sendEmail(to, null, subject, body, false);
    }
    
    /**
     * Envoie un email avec copie via Gmail API
     */
    public CompletableFuture<Boolean> sendEmail(String to, String cc, String subject, String body, boolean isHtml) {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                AppLogger.error("Service Gmail non authentifié");
                return false;
            }
            
            if (!config.isGmailActif()) {
                AppLogger.warn("Service Gmail désactivé dans la configuration");
                return false;
            }
            
            try {
                AppLogger.info("gmail", "Envoi email à: " + to + " - Sujet: " + subject);
                
                String rawMessage = buildRawMessage(to, cc, subject, body, isHtml);
                String encodedMessage = Base64.getUrlEncoder().encodeToString(rawMessage.getBytes());
                
                String jsonBody = String.format("{\"raw\":\"%s\"}", encodedMessage);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GMAIL_API_URL + "/users/me/messages/send"))
                        .header("Authorization", "Bearer " + authService.getValidAccessToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    AppLogger.info("gmail", "Email envoyé avec succès");
                    return true;
                } else {
                    AppLogger.error("Erreur envoi email Gmail: " + response.statusCode() + " - " + response.body());
                    return false;
                }
                
            } catch (Exception e) {
                AppLogger.error("Erreur envoi email Gmail: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Envoie un email de notification d'intervention
     */
    public CompletableFuture<Boolean> sendInterventionNotification(String clientEmail, String clientNom, 
                                                                   String technicienNom, String dateIntervention, 
                                                                   String typeIntervention) {
        String subject = "Confirmation d'intervention MAGSAV - " + dateIntervention;
        String body = buildInterventionEmailBody(clientNom, technicienNom, dateIntervention, typeIntervention);
        
        return sendEmail(clientEmail, null, subject, body, true);
    }
    
    /**
     * Envoie un email de rappel d'intervention
     */
    public CompletableFuture<Boolean> sendInterventionReminder(String clientEmail, String clientNom, 
                                                               String technicienNom, String dateIntervention, 
                                                               String heureIntervention) {
        String subject = "Rappel intervention MAGSAV - Demain " + heureIntervention;
        String body = buildReminderEmailBody(clientNom, technicienNom, dateIntervention, heureIntervention);
        
        return sendEmail(clientEmail, null, subject, body, true);
    }
    
    /**
     * Envoie un email de confirmation de commande
     */
    public CompletableFuture<Boolean> sendOrderConfirmation(String fournisseurEmail, String numeroCommande, 
                                                            String montantTotal, String detailsCommande) {
        String subject = "Commande MAGSAV #" + numeroCommande;
        String body = buildOrderEmailBody(numeroCommande, montantTotal, detailsCommande);
        
        return sendEmail(fournisseurEmail, null, subject, body, true);
    }
    
    /**
     * Teste la connexion Gmail
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            if (!authService.isAuthenticated()) {
                return false;
            }
            
            try {
                AppLogger.info("gmail", "Test de connexion Gmail API");
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GMAIL_API_URL + "/users/me/profile"))
                        .header("Authorization", "Bearer " + authService.getValidAccessToken())
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                boolean success = response.statusCode() == 200;
                AppLogger.info("gmail", "Test connexion Gmail: " + (success ? "OK" : "ECHEC"));
                
                return success;
                
            } catch (Exception e) {
                AppLogger.error("Erreur test connexion Gmail: " + e.getMessage());
                return false;
            }
        });
    }
    
    // Méthodes privées pour construire les messages
    
    private String buildRawMessage(String to, String cc, String subject, String body, boolean isHtml) {
        StringBuilder message = new StringBuilder();
        
        // En-têtes
        message.append("To: ").append(to).append("\r\n");
        if (cc != null && !cc.isEmpty()) {
            message.append("Cc: ").append(cc).append("\r\n");
        }
        message.append("From: ").append(config.getEmailExpéditeur()).append("\r\n");
        message.append("Subject: ").append(subject).append("\r\n");
        
        // Type de contenu
        if (isHtml) {
            message.append("Content-Type: text/html; charset=UTF-8\r\n");
        } else {
            message.append("Content-Type: text/plain; charset=UTF-8\r\n");
        }
        
        message.append("\r\n");
        
        // Corps du message
        message.append(body);
        
        // Signature si configurée
        if (!config.getSignatureEmail().isEmpty()) {
            message.append("\r\n\r\n");
            if (isHtml) {
                message.append("<br><br>").append(config.getSignatureEmail());
            } else {
                message.append("\n\n").append(config.getSignatureEmail());
            }
        }
        
        return message.toString();
    }
    
    private String buildInterventionEmailBody(String clientNom, String technicienNom, 
                                              String dateIntervention, String typeIntervention) {
        return String.format("""
            <html>
            <body>
                <h2>Confirmation d'intervention MAGSAV</h2>
                
                <p>Bonjour <strong>%s</strong>,</p>
                
                <p>Nous confirmons votre intervention :</p>
                
                <table border="1" style="border-collapse: collapse; margin: 20px 0;">
                    <tr>
                        <td style="padding: 8px; background-color: #f0f0f0;"><strong>Type d'intervention</strong></td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; background-color: #f0f0f0;"><strong>Date</strong></td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; background-color: #f0f0f0;"><strong>Technicien</strong></td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                </table>
                
                <p>Nous vous remercions de votre confiance.</p>
                
                <p>Cordialement,<br>
                L'équipe MAGSAV</p>
            </body>
            </html>
            """, clientNom, typeIntervention, dateIntervention, technicienNom);
    }
    
    private String buildReminderEmailBody(String clientNom, String technicienNom, 
                                          String dateIntervention, String heureIntervention) {
        return String.format("""
            <html>
            <body>
                <h2>🔔 Rappel intervention MAGSAV</h2>
                
                <p>Bonjour <strong>%s</strong>,</p>
                
                <p>Nous vous rappelons votre intervention prévue <strong>demain %s à %s</strong>.</p>
                
                <p>Votre technicien <strong>%s</strong> sera présent à l'heure convenue.</p>
                
                <p>Si vous avez des questions ou devez reporter l'intervention, 
                merci de nous contacter rapidement.</p>
                
                <p>À demain !<br>
                L'équipe MAGSAV</p>
            </body>
            </html>
            """, clientNom, dateIntervention, heureIntervention, technicienNom);
    }
    
    private String buildOrderEmailBody(String numeroCommande, String montantTotal, String detailsCommande) {
        return String.format("""
            <html>
            <body>
                <h2>Nouvelle commande MAGSAV</h2>
                
                <p>Bonjour,</p>
                
                <p>Nous vous adressons une nouvelle commande :</p>
                
                <table border="1" style="border-collapse: collapse; margin: 20px 0;">
                    <tr>
                        <td style="padding: 8px; background-color: #f0f0f0;"><strong>Numéro de commande</strong></td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; background-color: #f0f0f0;"><strong>Montant total</strong></td>
                        <td style="padding: 8px;">%s</td>
                    </tr>
                </table>
                
                <h3>Détails de la commande :</h3>
                <div style="border: 1px solid #ccc; padding: 10px; margin: 10px 0;">
                    %s
                </div>
                
                <p>Merci de nous confirmer la réception et les délais de livraison.</p>
                
                <p>Cordialement,<br>
                L'équipe MAGSAV</p>
            </body>
            </html>
            """, numeroCommande, montantTotal, detailsCommande);
    }
    
    // Getters
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }
    
    public GoogleServicesConfig getConfig() {
        return config;
    }
}