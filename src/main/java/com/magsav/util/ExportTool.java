package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.service.SimpleExportService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Desktop;

/**
 * Outil en ligne de commande pour tester les exports
 */
public class ExportTool {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        try {
            // Initialisation des dépendances
            DB.init();
            ProductRepository productRepo = new ProductRepository();
            InterventionRepository interventionRepo = new InterventionRepository();
            SimpleExportService exportService = new SimpleExportService(productRepo, interventionRepo);
            
            // Configuration des callbacks
            exportService.setLogCallback(message -> System.out.println("📝 " + message));
            exportService.setProgressCallback(progress -> {
                int percent = (int) (progress * 100);
                System.out.printf("\r🔄 Progression: %d%%", percent);
                if (progress >= 1.0) {
                    System.out.println();
                }
            });
            
            Path outputDir = Paths.get(System.getProperty("user.home"), "Desktop", "exports_magsav");
            
            switch (args[0].toLowerCase()) {
                case "product":
                    if (args.length < 2) {
                        System.err.println("❌ Usage: product <id>");
                        return;
                    }
                    exportProduct(exportService, Long.parseLong(args[1]), outputDir);
                    break;
                    
                case "all":
                    exportAllProducts(exportService, outputDir);
                    break;
                    
                case "stock":
                    exportStockReport(exportService, outputDir);
                    break;
                    
                default:
                    System.err.println("❌ Commande inconnue: " + args[0]);
                    printUsage();
                    System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void exportProduct(SimpleExportService service, long productId, Path outputDir) {
        System.out.println("🚀 Export du produit ID: " + productId);
        try {
            Path htmlFile = service.exportProductToHtml(productId, outputDir).get();
            System.out.println("✅ Export terminé: " + htmlFile);
            openFile(htmlFile);
        } catch (Exception e) {
            System.err.println("❌ Erreur export produit: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void exportAllProducts(SimpleExportService service, Path outputDir) {
        System.out.println("🚀 Export de tous les produits");
        try {
            Path htmlFile = service.exportAllProductsToHtml(outputDir).get();
            System.out.println("✅ Export complet terminé: " + htmlFile);
            openFile(htmlFile);
        } catch (Exception e) {
            System.err.println("❌ Erreur export complet: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void exportStockReport(SimpleExportService service, Path outputDir) {
        System.out.println("🚀 Génération du rapport de stock");
        try {
            Path htmlFile = service.exportStockReport(outputDir).get();
            System.out.println("✅ Rapport de stock généré: " + htmlFile);
            openFile(htmlFile);
        } catch (Exception e) {
            System.err.println("❌ Erreur génération rapport: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void openFile(Path filePath) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(filePath.toFile());
                System.out.println("🌐 Ouverture du fichier dans le navigateur...");
            } else {
                System.out.println("💡 Ouvrez manuellement: " + filePath);
            }
        } catch (Exception e) {
            System.out.println("💡 Fichier créé: " + filePath);
        }
    }
    
    private static void printUsage() {
        System.out.println("📋 Outil d'export MAGSAV");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java ... ExportTool product <id>  - Exporte un produit spécifique");
        System.out.println("  java ... ExportTool all           - Exporte tous les produits");
        System.out.println("  java ... ExportTool stock         - Génère un rapport de stock");
        System.out.println();
        System.out.println("Les fichiers sont générés dans ~/Desktop/exports_magsav/");
    }
}