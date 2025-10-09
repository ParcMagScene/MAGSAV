package com.magsav.service;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.util.AppLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service intégré pour export et partage par email
 */
public class ExportEmailService {
    
    private final SimpleExportService exportService;
    private final EmailService emailService;
    
    private Consumer<String> logCallback;
    private Consumer<Double> progressCallback;
    
    public ExportEmailService(ProductRepository productRepo, InterventionRepository interventionRepo) {
        this.exportService = new SimpleExportService(productRepo, interventionRepo);
        this.emailService = new EmailService();
        
        // Configuration des callbacks pour les services internes
        setupServiceCallbacks();
    }
    
    public void setEmailConfiguration(String username, String password) {
        emailService.setConfiguration(username, password);
    }
    
    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
        setupServiceCallbacks();
    }
    
    public void setProgressCallback(Consumer<Double> callback) {
        this.progressCallback = callback;
        setupServiceCallbacks();
    }
    
    private void setupServiceCallbacks() {
        if (logCallback != null) {
            exportService.setLogCallback(logCallback);
            emailService.setLogCallback(logCallback);
        }
        
        if (progressCallback != null) {
            // Répartition du progrès : 70% export, 30% email
            exportService.setProgressCallback(progress -> progressCallback.accept(progress * 0.7));
            emailService.setProgressCallback(progress -> progressCallback.accept(0.7 + progress * 0.3));
        }
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
     * Génère et envoie par email un rapport de produit
     */
    public CompletableFuture<Boolean> exportAndEmailProduct(long productId, String recipientEmail, String productName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("🚀 Génération et envoi du rapport produit: " + productName);
                updateProgress(0.1);
                
                // Génération du rapport
                Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "magsav_email_temp");
                Path htmlReport = exportService.exportProductToHtml(productId, outputDir).get();
                updateProgress(0.7);
                
                // Envoi par email
                boolean emailSent = emailService.sendProductReport(recipientEmail, productName, htmlReport).get();
                updateProgress(1.0);
                
                if (emailSent) {
                    log("✅ Rapport produit envoyé avec succès à: " + recipientEmail);
                } else {
                    log("❌ Échec envoi du rapport produit");
                }
                
                return emailSent;
                
            } catch (Exception e) {
                log("❌ Erreur export/email produit: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et envoie par email un rapport de stock
     */
    public CompletableFuture<Boolean> exportAndEmailStockReport(String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📊 Génération et envoi du rapport de stock");
                updateProgress(0.1);
                
                // Génération du rapport
                Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "magsav_email_temp");
                Path htmlReport = exportService.exportStockReport(outputDir).get();
                updateProgress(0.7);
                
                // Envoi par email
                boolean emailSent = emailService.sendStockReport(recipientEmail, htmlReport).get();
                updateProgress(1.0);
                
                if (emailSent) {
                    log("✅ Rapport de stock envoyé avec succès à: " + recipientEmail);
                } else {
                    log("❌ Échec envoi du rapport de stock");
                }
                
                return emailSent;
                
            } catch (Exception e) {
                log("❌ Erreur export/email stock: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et envoie par email un export complet
     */
    public CompletableFuture<Boolean> exportAndEmailCompleteDatabase(String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📦 Génération et envoi de l'export complet");
                updateProgress(0.1);
                
                // Génération de l'export
                Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "magsav_email_temp");
                Path htmlExport = exportService.exportAllProductsToHtml(outputDir).get();
                updateProgress(0.7);
                
                // Envoi par email
                boolean emailSent = emailService.sendCompleteExport(recipientEmail, htmlExport).get();
                updateProgress(1.0);
                
                if (emailSent) {
                    log("✅ Export complet envoyé avec succès à: " + recipientEmail);
                } else {
                    log("❌ Échec envoi de l'export complet");
                }
                
                return emailSent;
                
            } catch (Exception e) {
                log("❌ Erreur export/email complet: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Test de la configuration email
     */
    public CompletableFuture<Boolean> testEmailConfiguration() {
        return emailService.testEmailConfiguration();
    }
    
    /**
     * Validation d'une adresse email
     */
    public static boolean isValidEmail(String email) {
        return EmailService.isValidEmail(email);
    }
    
    /**
     * Export seul (sans email) pour fichiers locaux
     */
    public CompletableFuture<Path> exportProductToFile(long productId, Path outputDir) {
        return exportService.exportProductToHtml(productId, outputDir);
    }
    
    public CompletableFuture<Path> exportStockReportToFile(Path outputDir) {
        return exportService.exportStockReport(outputDir);
    }
    
    public CompletableFuture<Path> exportCompleteDatabaseToFile(Path outputDir) {
        return exportService.exportAllProductsToHtml(outputDir);
    }
}