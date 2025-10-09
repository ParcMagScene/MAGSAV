package com.magsav.service;

import com.magsav.util.AppLogger;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service d'envoi d'emails via Gmail
 */
public class EmailService {
    
    private String smtpHost = "smtp.gmail.com";
    private int smtpPort = 587;
    private String username;
    private String password; // Mot de passe d'application Gmail
    
    private Consumer<String> logCallback;
    private Consumer<Double> progressCallback;
    
    public EmailService() {
        // Configuration par défaut Gmail
    }
    
    public void setConfiguration(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }
    
    public void setProgressCallback(Consumer<Double> callback) {
        this.progressCallback = callback;
    }
    
    private void log(String message) {
        AppLogger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
    
    private void updateProgress(double progress) {
        if (progressCallback != null) {
            progressCallback.accept(progress);
        }
    }
    
    /**
     * Envoie un email simple
     */
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String body) {
        return sendEmailWithAttachment(to, subject, body, null);
    }
    
    /**
     * Envoie un email avec pièce jointe
     */
    public CompletableFuture<Boolean> sendEmailWithAttachment(String to, String subject, String body, Path attachmentPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📧 Préparation de l'email pour: " + to);
                updateProgress(0.1);
                
                if (username == null || password == null) {
                    throw new RuntimeException("Configuration email manquante. Utilisez setConfiguration()");
                }
                
                // Configuration des propriétés SMTP
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", smtpPort);
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                
                updateProgress(0.3);
                
                // Création de la session
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
                
                // Création du message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                
                updateProgress(0.5);
                
                if (attachmentPath != null && attachmentPath.toFile().exists()) {
                    // Message avec pièce jointe
                    Multipart multipart = new MimeMultipart();
                    
                    // Corps du message
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setContent(body, "text/html; charset=utf-8");
                    multipart.addBodyPart(messageBodyPart);
                    
                    // Pièce jointe
                    BodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachmentPath.toFile());
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(attachmentPath.getFileName().toString());
                    multipart.addBodyPart(attachmentPart);
                    
                    message.setContent(multipart);
                } else {
                    // Message simple
                    message.setContent(body, "text/html; charset=utf-8");
                }
                
                updateProgress(0.8);
                
                // Envoi
                Transport.send(message);
                updateProgress(1.0);
                
                log("✅ Email envoyé avec succès à: " + to);
                return true;
                
            } catch (Exception e) {
                log("❌ Erreur envoi email: " + e.getMessage());
                throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Envoie un rapport de produit par email
     */
    public CompletableFuture<Boolean> sendProductReport(String to, String productName, Path htmlReport) {
        String subject = "MAGSAV - Fiche produit: " + productName;
        String body = buildProductEmailBody(productName);
        
        return sendEmailWithAttachment(to, subject, body, htmlReport);
    }
    
    /**
     * Envoie un rapport de stock par email
     */
    public CompletableFuture<Boolean> sendStockReport(String to, Path htmlReport) {
        String subject = "MAGSAV - Rapport de stock";
        String body = buildStockEmailBody();
        
        return sendEmailWithAttachment(to, subject, body, htmlReport);
    }
    
    /**
     * Envoie un export complet par email
     */
    public CompletableFuture<Boolean> sendCompleteExport(String to, Path htmlExport) {
        String subject = "MAGSAV - Export complet de la base de données";
        String body = buildCompleteExportEmailBody();
        
        return sendEmailWithAttachment(to, subject, body, htmlExport);
    }
    
    /**
     * Test de configuration email
     */
    public CompletableFuture<Boolean> testEmailConfiguration() {
        String testSubject = "MAGSAV - Test de configuration";
        String testBody = buildTestEmailBody();
        
        return sendEmail(username, testSubject, testBody);
    }
    
    private String buildProductEmailBody(String productName) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; margin: 20px; color: #333;">
                <div style="background: linear-gradient(135deg, #007cba, #0099d6); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                    <h1 style="margin: 0;">📋 MAGSAV - Fiche Produit</h1>
                    <p style="margin: 5px 0 0 0; opacity: 0.9;">Système de gestion de matériel audio/vidéo</p>
                </div>
                
                <h2>Produit: %s</h2>
                
                <p>Vous trouverez ci-joint la fiche détaillée du produit demandé.</p>
                
                <div style="background: #f8f9fa; padding: 15px; border-left: 4px solid #007cba; margin: 20px 0;">
                    <h3>📎 Pièce jointe</h3>
                    <p>Le rapport HTML contient toutes les informations détaillées du produit, y compris:</p>
                    <ul>
                        <li>Informations techniques complètes</li>
                        <li>Historique des interventions</li>
                        <li>Statut et situation actuelle</li>
                        <li>Données d'achat et client</li>
                    </ul>
                </div>
                
                <hr style="margin: 30px 0; border: 1px solid #ddd;">
                
                <footer style="text-align: center; color: #666; font-size: 0.9em;">
                    <p>Généré automatiquement par MAGSAV</p>
                    <p>📧 Email envoyé le %s</p>
                </footer>
            </body>
            </html>
            """.formatted(productName, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
    }
    
    private String buildStockEmailBody() {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; margin: 20px; color: #333;">
                <div style="background: linear-gradient(135deg, #007cba, #0099d6); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                    <h1 style="margin: 0;">📊 MAGSAV - Rapport de Stock</h1>
                    <p style="margin: 5px 0 0 0; opacity: 0.9;">Système de gestion de matériel audio/vidéo</p>
                </div>
                
                <p>Vous trouverez ci-joint le rapport de stock complet de votre inventaire.</p>
                
                <div style="background: #f8f9fa; padding: 15px; border-left: 4px solid #007cba; margin: 20px 0;">
                    <h3>📎 Contenu du rapport</h3>
                    <p>Le rapport HTML joint contient:</p>
                    <ul>
                        <li>Résumé statistique du stock</li>
                        <li>Répartition par situation (Disponible, En utilisation, Maintenance)</li>
                        <li>Liste détaillée de tous les produits</li>
                        <li>Informations de codes, numéros de série et fabricants</li>
                    </ul>
                </div>
                
                <div style="background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>💡 Utilisation</h3>
                    <p>Ce rapport peut être ouvert dans n'importe quel navigateur web et imprimé si nécessaire.</p>
                </div>
                
                <hr style="margin: 30px 0; border: 1px solid #ddd;">
                
                <footer style="text-align: center; color: #666; font-size: 0.9em;">
                    <p>Généré automatiquement par MAGSAV</p>
                    <p>📧 Email envoyé le %s</p>
                </footer>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
    }
    
    private String buildCompleteExportEmailBody() {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; margin: 20px; color: #333;">
                <div style="background: linear-gradient(135deg, #007cba, #0099d6); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                    <h1 style="margin: 0;">📦 MAGSAV - Export Complet</h1>
                    <p style="margin: 5px 0 0 0; opacity: 0.9;">Système de gestion de matériel audio/vidéo</p>
                </div>
                
                <p>Vous trouverez ci-joint l'export complet de votre base de données MAGSAV.</p>
                
                <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>⚠️ Export Complet</h3>
                    <p><strong>Attention:</strong> Ce fichier contient toutes les données de votre système, incluant toutes les fiches produits avec leur historique complet.</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 15px; border-left: 4px solid #007cba; margin: 20px 0;">
                    <h3>📎 Contenu de l'export</h3>
                    <p>L'export HTML joint contient:</p>
                    <ul>
                        <li>Toutes les fiches produits avec détails complets</li>
                        <li>Historiques d'interventions pour chaque produit</li>
                        <li>Informations techniques et commerciales</li>
                        <li>Données clients et situations</li>
                        <li>Formatage optimisé pour impression</li>
                    </ul>
                </div>
                
                <div style="background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>💡 Recommandations</h3>
                    <ul>
                        <li>Sauvegardez ce fichier en lieu sûr</li>
                        <li>N'ouvrez que dans un environnement sécurisé</li>
                        <li>Peut être utilisé pour archivage ou migration</li>
                    </ul>
                </div>
                
                <hr style="margin: 30px 0; border: 1px solid #ddd;">
                
                <footer style="text-align: center; color: #666; font-size: 0.9em;">
                    <p>Généré automatiquement par MAGSAV</p>
                    <p>📧 Email envoyé le %s</p>
                </footer>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
    }
    
    private String buildTestEmailBody() {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; margin: 20px; color: #333;">
                <div style="background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                    <h1 style="margin: 0;">✅ Test de Configuration Email</h1>
                    <p style="margin: 5px 0 0 0; opacity: 0.9;">MAGSAV - Système de gestion de matériel audio/vidéo</p>
                </div>
                
                <h2>Configuration Email Validée</h2>
                
                <p>Si vous recevez cet email, la configuration de votre service email MAGSAV fonctionne correctement.</p>
                
                <div style="background: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>✅ Test réussi</h3>
                    <p>Votre système peut maintenant envoyer:</p>
                    <ul>
                        <li>Fiches produits individuelles</li>
                        <li>Rapports de stock</li>
                        <li>Exports complets de base de données</li>
                        <li>Notifications et alertes</li>
                    </ul>
                </div>
                
                <div style="background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>🔧 Configuration utilisée</h3>
                    <ul>
                        <li><strong>Serveur SMTP:</strong> Gmail (smtp.gmail.com:587)</li>
                        <li><strong>Sécurité:</strong> TLS activé</li>
                        <li><strong>Date du test:</strong> %s</li>
                    </ul>
                </div>
                
                <hr style="margin: 30px 0; border: 1px solid #ddd;">
                
                <footer style="text-align: center; color: #666; font-size: 0.9em;">
                    <p>Test automatique généré par MAGSAV</p>
                </footer>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")));
    }
    
    /**
     * Valide une adresse email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}