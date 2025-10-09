package com.magsav.util;

import com.magsav.service.ImageMaintenanceService;

/**
 * Outil en ligne de commande pour la maintenance des images
 * Usage: java com.magsav.util.ImageMaintenanceTool [normalize|duplicates|full]
 */
public class ImageMaintenanceTool {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        
        ImageMaintenanceService service = new ImageMaintenanceService();
        String command = args[0].toLowerCase();
        
        try {
            switch (command) {
                case "normalize":
                    System.out.println("=== NORMALISATION DES IMAGES ===");
                    var normalizeReport = service.normalizeExistingImages();
                    System.out.println(normalizeReport);
                    break;
                    
                case "normalize-logos":
                    System.out.println("=== NORMALISATION DES LOGOS ===");
                    var logoReport = service.normalizeExistingLogos();
                    System.out.println(logoReport);
                    break;
                    
                case "normalize-all":
                    System.out.println("=== NORMALISATION IMAGES & LOGOS ===");
                    var allImagesReport = service.normalizeExistingImages();
                    var allLogosReport = service.normalizeExistingLogos();
                    System.out.println("--- IMAGES ---");
                    System.out.println(allImagesReport);
                    System.out.println("--- LOGOS ---");
                    System.out.println(allLogosReport);
                    break;
                    
                case "duplicates":
                    System.out.println("=== SUPPRESSION DES DOUBLONS ===");
                    System.out.println("⚠️  ATTENTION: Cette opération supprime définitivement les fichiers dupliqués!");
                    System.out.print("Voulez-vous continuer? (o/N): ");
                    
                    String response = System.console() != null ? 
                        System.console().readLine() : "n";
                    
                    if ("o".equalsIgnoreCase(response) || "oui".equalsIgnoreCase(response)) {
                        var duplicateReport = service.detectAndRemoveDuplicates();
                        System.out.println(duplicateReport);
                    } else {
                        System.out.println("Opération annulée.");
                    }
                    break;
                    
                case "full":
                    System.out.println("=== MAINTENANCE COMPLÈTE ===");
                    System.out.println("Cette opération va normaliser les images ET supprimer les doublons.");
                    System.out.print("Voulez-vous continuer? (o/N): ");
                    
                    String fullResponse = System.console() != null ? 
                        System.console().readLine() : "n";
                    
                    if ("o".equalsIgnoreCase(fullResponse) || "oui".equalsIgnoreCase(fullResponse)) {
                        var fullReport = service.performFullMaintenance();
                        System.out.println(fullReport);
                    } else {
                        System.out.println("Opération annulée.");
                    }
                    break;
                    
                default:
                    System.err.println("Commande inconnue: " + command);
                    printUsage();
                    System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la maintenance: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java com.magsav.util.ImageMaintenanceTool <commande>");
        System.out.println();
        System.out.println("Commandes disponibles:");
        System.out.println("  normalize       - Normalise toutes les images existantes");
        System.out.println("  normalize-logos - Normalise tous les logos existants");
        System.out.println("  normalize-all   - Normalise images et logos");
        System.out.println("  duplicates      - Détecte et supprime les images dupliquées");
        System.out.println("  full            - Effectue la maintenance complète (normalize + duplicates)");
        System.out.println();
        System.out.println("Exemples:");
        System.out.println("  java com.magsav.util.ImageMaintenanceTool normalize");
        System.out.println("  java com.magsav.util.ImageMaintenanceTool normalize-logos");
        System.out.println("  java com.magsav.util.ImageMaintenanceTool normalize-all");
        System.out.println("  java com.magsav.util.ImageMaintenanceTool duplicates");
        System.out.println("  java com.magsav.util.ImageMaintenanceTool full");
    }
}