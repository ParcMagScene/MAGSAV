package com.magsav.service;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.util.AppLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service complet de partage MAGSAV : Export, Email et Impression
 */
public class ShareService {
    
    private final SimpleExportService exportService;
    private final EmailService emailService;
    private final PrintService printService;
    
    private final Path defaultOutputDir;
    
    private Consumer<String> logCallback;
    private Consumer<Double> progressCallback;
    
    public ShareService(ProductRepository productRepo, InterventionRepository interventionRepo) {
        this.exportService = new SimpleExportService(productRepo, interventionRepo);
        this.emailService = new EmailService();
        this.printService = new PrintService();
        
        this.defaultOutputDir = Paths.get(System.getProperty("user.home"), "Desktop", "magsav_shares");
        
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
            printService.setLogCallback(logCallback);
        }
        
        if (progressCallback != null) {
            exportService.setProgressCallback(progress -> progressCallback.accept(progress * 0.5));
            emailService.setProgressCallback(progress -> progressCallback.accept(0.5 + progress * 0.3));
            printService.setProgressCallback(progress -> progressCallback.accept(0.8 + progress * 0.2));
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
    
    // ===================== EXPORT SEUL =====================
    
    /**
     * Export d'un produit en fichier local
     */
    public CompletableFuture<Path> exportProduct(long productId) {
        return exportService.exportProductToHtml(productId, defaultOutputDir);
    }
    
    /**
     * Export du rapport de stock en fichier local
     */
    public CompletableFuture<Path> exportStockReport() {
        return exportService.exportStockReport(defaultOutputDir);
    }
    
    /**
     * Export complet de la base de données en fichier local
     */
    public CompletableFuture<Path> exportCompleteDatabase() {
        return exportService.exportAllProductsToHtml(defaultOutputDir);
    }
    
    // ===================== EMAIL =====================
    
    /**
     * Génère et envoie un rapport de produit par email
     */
    public CompletableFuture<Boolean> emailProduct(long productId, String recipientEmail, String productName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📧 Génération et envoi email du produit: " + productName);
                updateProgress(0.1);
                
                Path htmlFile = exportService.exportProductToHtml(productId, defaultOutputDir).get();
                updateProgress(0.5);
                
                boolean sent = emailService.sendProductReport(recipientEmail, productName, htmlFile).get();
                updateProgress(1.0);
                
                return sent;
                
            } catch (Exception e) {
                log("❌ Erreur email produit: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et envoie un rapport de stock par email
     */
    public CompletableFuture<Boolean> emailStockReport(String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📧 Génération et envoi email du rapport de stock");
                updateProgress(0.1);
                
                Path htmlFile = exportService.exportStockReport(defaultOutputDir).get();
                updateProgress(0.5);
                
                boolean sent = emailService.sendStockReport(recipientEmail, htmlFile).get();
                updateProgress(1.0);
                
                return sent;
                
            } catch (Exception e) {
                log("❌ Erreur email stock: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et envoie l'export complet par email
     */
    public CompletableFuture<Boolean> emailCompleteDatabase(String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📧 Génération et envoi email de l'export complet");
                updateProgress(0.1);
                
                Path htmlFile = exportService.exportAllProductsToHtml(defaultOutputDir).get();
                updateProgress(0.5);
                
                boolean sent = emailService.sendCompleteExport(recipientEmail, htmlFile).get();
                updateProgress(1.0);
                
                return sent;
                
            } catch (Exception e) {
                log("❌ Erreur email export complet: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    // ===================== IMPRESSION =====================
    
    /**
     * Génère et imprime un rapport de produit
     */
    public CompletableFuture<Boolean> printProduct(long productId) {
        return printService.printProductReport(productId, exportService);
    }
    
    /**
     * Génère et imprime un rapport de stock
     */
    public CompletableFuture<Boolean> printStockReport() {
        return printService.printStockReport(exportService);
    }
    
    /**
     * Génère et imprime l'export complet
     */
    public CompletableFuture<Boolean> printCompleteDatabase() {
        return printService.printCompleteDatabase(exportService);
    }
    
    // ===================== ACTIONS COMBINÉES =====================
    
    /**
     * Export + Email + Impression d'un produit
     */
    public CompletableFuture<ShareResult> shareProductComplete(long productId, String productName, String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("🚀 Partage complet du produit: " + productName);
                updateProgress(0.1);
                
                ShareResult result = new ShareResult();
                
                // 1. Export
                Path exportFile = exportService.exportProductToHtml(productId, defaultOutputDir).get();
                result.exportFile = exportFile;
                result.exportSuccess = true;
                log("✅ Export réussi: " + exportFile);
                updateProgress(0.4);
                
                // 2. Email (si adresse fournie)
                if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
                    try {
                        boolean emailSent = emailService.sendProductReport(recipientEmail, productName, exportFile).get();
                        result.emailSuccess = emailSent;
                        if (emailSent) {
                            log("✅ Email envoyé à: " + recipientEmail);
                        }
                    } catch (Exception e) {
                        log("⚠️ Échec email: " + e.getMessage());
                        result.emailSuccess = false;
                    }
                }
                updateProgress(0.7);
                
                // 3. Impression
                try {
                    boolean printed = printService.showPrintDialog(exportFile).get();
                    result.printSuccess = printed;
                    if (printed) {
                        log("✅ Fichier ouvert pour impression");
                    }
                } catch (Exception e) {
                    log("⚠️ Échec impression: " + e.getMessage());
                    result.printSuccess = false;
                }
                updateProgress(1.0);
                
                log("🎯 Partage complet terminé");
                return result;
                
            } catch (Exception e) {
                log("❌ Erreur partage complet: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Export + Email + Impression du rapport de stock
     */
    public CompletableFuture<ShareResult> shareStockReportComplete(String recipientEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("🚀 Partage complet du rapport de stock");
                updateProgress(0.1);
                
                ShareResult result = new ShareResult();
                
                // 1. Export
                Path exportFile = exportService.exportStockReport(defaultOutputDir).get();
                result.exportFile = exportFile;
                result.exportSuccess = true;
                log("✅ Rapport de stock exporté: " + exportFile);
                updateProgress(0.4);
                
                // 2. Email (si adresse fournie)
                if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
                    try {
                        boolean emailSent = emailService.sendStockReport(recipientEmail, exportFile).get();
                        result.emailSuccess = emailSent;
                        if (emailSent) {
                            log("✅ Rapport envoyé par email à: " + recipientEmail);
                        }
                    } catch (Exception e) {
                        log("⚠️ Échec email: " + e.getMessage());
                        result.emailSuccess = false;
                    }
                }
                updateProgress(0.7);
                
                // 3. Impression
                try {
                    boolean printed = printService.showPrintDialog(exportFile).get();
                    result.printSuccess = printed;
                    if (printed) {
                        log("✅ Rapport ouvert pour impression");
                    }
                } catch (Exception e) {
                    log("⚠️ Échec impression: " + e.getMessage());
                    result.printSuccess = false;
                }
                updateProgress(1.0);
                
                log("🎯 Partage complet du rapport terminé");
                return result;
                
            } catch (Exception e) {
                log("❌ Erreur partage rapport: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    // ===================== UTILITAIRES =====================
    
    /**
     * Test de la configuration email
     */
    public CompletableFuture<Boolean> testEmailConfiguration() {
        return emailService.testEmailConfiguration();
    }
    
    /**
     * Validation d'adresse email
     */
    public static boolean isValidEmail(String email) {
        return EmailService.isValidEmail(email);
    }
    
    /**
     * Vérification support impression
     */
    public boolean isPrintingSupported() {
        return printService.isPrintingSupported();
    }
    
    /**
     * Liste des imprimantes disponibles
     */
    public String[] getAvailablePrinters() {
        return printService.getAvailablePrinters();
    }
    
    /**
     * Ouverture du dossier des exports
     */
    public CompletableFuture<Boolean> openExportsFolder() {
        return printService.openExportsFolder(defaultOutputDir);
    }
    
    /**
     * Récupération du dossier d'export par défaut
     */
    public Path getDefaultOutputDirectory() {
        return defaultOutputDir;
    }
    
    // ===================== RÉSULTAT DE PARTAGE =====================
    
    /**
     * Résultat d'une opération de partage complète
     */
    public static class ShareResult {
        public Path exportFile;
        public boolean exportSuccess = false;
        public boolean emailSuccess = false;
        public boolean printSuccess = false;
        
        public boolean isFullySuccessful() {
            return exportSuccess && emailSuccess && printSuccess;
        }
        
        public boolean isPartiallySuccessful() {
            return exportSuccess && (emailSuccess || printSuccess);
        }
        
        public int getSuccessCount() {
            int count = 0;
            if (exportSuccess) count++;
            if (emailSuccess) count++;
            if (printSuccess) count++;
            return count;
        }
        
        @Override
        public String toString() {
            return String.format("ShareResult{export=%s, email=%s, print=%s, file=%s}", 
                exportSuccess, emailSuccess, printSuccess, 
                exportFile != null ? exportFile.getFileName() : "none");
        }
    }
}