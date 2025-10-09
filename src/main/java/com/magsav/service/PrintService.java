package com.magsav.service;

import com.magsav.util.AppLogger;

import java.awt.*;
import javax.print.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service d'impression native pour les rapports MAGSAV
 */
public class PrintService {
    
    private Consumer<String> logCallback;
    private Consumer<Double> progressCallback;
    
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
     * Imprime un fichier HTML en utilisant l'impression native du système
     */
    public CompletableFuture<Boolean> printHtmlFile(Path htmlFile, String jobName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("🖨️ Impression du fichier: " + htmlFile.getFileName());
                updateProgress(0.1);
                
                if (!htmlFile.toFile().exists()) {
                    throw new RuntimeException("Fichier introuvable: " + htmlFile);
                }
                
                updateProgress(0.3);
                
                // Utilisation du navigateur par défaut pour l'impression
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        log("📄 Ouverture du fichier pour impression...");
                        desktop.open(htmlFile.toFile());
                        updateProgress(0.8);
                        
                        // Petit délai pour permettre l'ouverture
                        Thread.sleep(2000);
                        
                        log("💡 Utilisez Ctrl+P dans votre navigateur pour imprimer");
                        updateProgress(1.0);
                        return true;
                    }
                }
                
                log("❌ Impression automatique non supportée sur ce système");
                return false;
                
            } catch (Exception e) {
                log("❌ Erreur impression: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et imprime un rapport de produit
     */
    public CompletableFuture<Boolean> printProductReport(long productId, SimpleExportService exportService) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📋 Génération et impression du rapport produit ID: " + productId);
                updateProgress(0.1);
                
                // Génération du rapport
                Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "magsav_print");
                Path htmlFile = exportService.exportProductToHtml(productId, tempDir).get();
                updateProgress(0.6);
                
                // Impression
                boolean printed = printHtmlFile(htmlFile, "MAGSAV - Produit #" + productId).get();
                updateProgress(1.0);
                
                if (printed) {
                    log("✅ Rapport produit prêt à imprimer");
                } else {
                    log("❌ Échec impression rapport produit");
                }
                
                return printed;
                
            } catch (Exception e) {
                log("❌ Erreur génération/impression produit: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et imprime un rapport de stock
     */
    public CompletableFuture<Boolean> printStockReport(SimpleExportService exportService) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📊 Génération et impression du rapport de stock");
                updateProgress(0.1);
                
                // Génération du rapport
                Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "magsav_print");
                Path htmlFile = exportService.exportStockReport(tempDir).get();
                updateProgress(0.6);
                
                // Impression
                boolean printed = printHtmlFile(htmlFile, "MAGSAV - Rapport de Stock").get();
                updateProgress(1.0);
                
                if (printed) {
                    log("✅ Rapport de stock prêt à imprimer");
                } else {
                    log("❌ Échec impression rapport de stock");
                }
                
                return printed;
                
            } catch (Exception e) {
                log("❌ Erreur génération/impression stock: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère et imprime l'export complet de la base de données
     */
    public CompletableFuture<Boolean> printCompleteDatabase(SimpleExportService exportService) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📦 Génération et impression de l'export complet");
                updateProgress(0.1);
                
                // Génération de l'export
                Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "magsav_print");
                Path htmlFile = exportService.exportAllProductsToHtml(tempDir).get();
                updateProgress(0.6);
                
                // Impression
                boolean printed = printHtmlFile(htmlFile, "MAGSAV - Base Complète").get();
                updateProgress(1.0);
                
                if (printed) {
                    log("✅ Export complet prêt à imprimer");
                } else {
                    log("❌ Échec impression export complet");
                }
                
                return printed;
                
            } catch (Exception e) {
                log("❌ Erreur génération/impression complète: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Vérifie si l'impression est supportée sur le système
     */
    public boolean isPrintingSupported() {
        return Desktop.isDesktopSupported() && 
               Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }
    
    /**
     * Affiche la boîte de dialogue d'impression native
     */
    public CompletableFuture<Boolean> showPrintDialog(Path htmlFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("🖨️ Ouverture de la boîte de dialogue d'impression");
                updateProgress(0.2);
                
                if (!isPrintingSupported()) {
                    throw new RuntimeException("Impression non supportée sur ce système");
                }
                
                updateProgress(0.5);
                
                // Ouverture du fichier avec l'application par défaut
                Desktop.getDesktop().open(htmlFile.toFile());
                updateProgress(0.8);
                
                // Instructions pour l'utilisateur
                log("💡 Le fichier s'ouvre dans votre navigateur");
                log("💡 Utilisez Ctrl+P (ou Cmd+P sur Mac) pour imprimer");
                log("💡 Configurez vos options d'impression dans la boîte de dialogue");
                updateProgress(1.0);
                
                return true;
                
            } catch (Exception e) {
                log("❌ Erreur ouverture impression: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Informations sur les imprimantes disponibles
     */
    public String[] getAvailablePrinters() {
        try {
            javax.print.PrintService[] javaPrintServices = PrintServiceLookup.lookupPrintServices(null, null);
            String[] printerNames = new String[javaPrintServices.length];
            
            for (int i = 0; i < javaPrintServices.length; i++) {
                printerNames[i] = javaPrintServices[i].getName();
            }
            
            log("🖨️ Imprimantes détectées: " + javaPrintServices.length);
            return printerNames;
            
        } catch (Exception e) {
            log("⚠️ Impossible de détecter les imprimantes: " + e.getMessage());
            return new String[0];
        }
    }
    
    /**
     * Ouvre le dossier des exports pour accès manuel
     */
    public CompletableFuture<Boolean> openExportsFolder(Path exportsFolder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📁 Ouverture du dossier des exports");
                updateProgress(0.3);
                
                if (!Desktop.isDesktopSupported()) {
                    throw new RuntimeException("Ouverture de dossier non supportée");
                }
                
                java.nio.file.Files.createDirectories(exportsFolder);
                updateProgress(0.6);
                
                Desktop.getDesktop().open(exportsFolder.toFile());
                updateProgress(1.0);
                
                log("✅ Dossier des exports ouvert");
                return true;
                
            } catch (Exception e) {
                log("❌ Erreur ouverture dossier: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}