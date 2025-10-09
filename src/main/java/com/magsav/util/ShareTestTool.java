package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.service.ShareService;
import com.magsav.service.ShareService.ShareResult;

import java.util.Scanner;

/**
 * Outil de test complet pour le système de partage MAGSAV
 */
public class ShareTestTool {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        try {
            // Initialisation
            DB.init();
            ProductRepository productRepo = new ProductRepository();
            InterventionRepository interventionRepo = new InterventionRepository();
            ShareService shareService = new ShareService(productRepo, interventionRepo);
            
            // Configuration des callbacks
            shareService.setLogCallback(message -> System.out.println("📋 " + message));
            shareService.setProgressCallback(progress -> {
                int percent = (int) (progress * 100);
                System.out.printf("\r🔄 Progression: %d%%", percent);
                if (progress >= 1.0) {
                    System.out.println();
                }
            });
            
            // Configuration email si nécessaire
            if (needsEmailConfig(args[0])) {
                setupEmailConfiguration(shareService);
            }
            
            switch (args[0].toLowerCase()) {
                case "export":
                    handleExportCommands(args, shareService);
                    break;
                    
                case "email":
                    handleEmailCommands(args, shareService);
                    break;
                    
                case "print":
                    handlePrintCommands(args, shareService);
                    break;
                    
                case "share":
                    handleShareCommands(args, shareService);
                    break;
                    
                case "info":
                    handleInfoCommands(shareService);
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
    
    private static boolean needsEmailConfig(String command) {
        return command.equalsIgnoreCase("email") || command.equalsIgnoreCase("share");
    }
    
    private static void setupEmailConfiguration(ShareService shareService) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("📧 Configuration Gmail requise");
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Mot de passe d'application: ");
            String password = scanner.nextLine().trim();
            
            shareService.setEmailConfiguration(email, password);
            System.out.println("✅ Configuration email définie");
        }
    }
    
    private static void handleExportCommands(String[] args, ShareService shareService) throws Exception {
        if (args.length < 2) {
            System.err.println("❌ Usage: export <product|stock|all>");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "product":
                if (args.length < 3) {
                    System.err.println("❌ Usage: export product <id>");
                    return;
                }
                long productId = Long.parseLong(args[2]);
                var productFile = shareService.exportProduct(productId).get();
                System.out.println("✅ Produit exporté: " + productFile);
                break;
                
            case "stock":
                var stockFile = shareService.exportStockReport().get();
                System.out.println("✅ Stock exporté: " + stockFile);
                break;
                
            case "all":
                var allFile = shareService.exportCompleteDatabase().get();
                System.out.println("✅ Export complet: " + allFile);
                break;
                
            default:
                System.err.println("❌ Type d'export inconnu: " + args[1]);
        }
    }
    
    private static void handleEmailCommands(String[] args, ShareService shareService) throws Exception {
        if (args.length < 3) {
            System.err.println("❌ Usage: email <product|stock|all> <email> [id]");
            return;
        }
        
        String email = args[2];
        
        switch (args[1].toLowerCase()) {
            case "product":
                if (args.length < 4) {
                    System.err.println("❌ Usage: email product <email> <id>");
                    return;
                }
                long productId = Long.parseLong(args[3]);
                boolean productSent = shareService.emailProduct(productId, email, "Produit #" + productId).get();
                System.out.println(productSent ? "✅ Email produit envoyé" : "❌ Échec email produit");
                break;
                
            case "stock":
                boolean stockSent = shareService.emailStockReport(email).get();
                System.out.println(stockSent ? "✅ Email stock envoyé" : "❌ Échec email stock");
                break;
                
            case "all":
                boolean allSent = shareService.emailCompleteDatabase(email).get();
                System.out.println(allSent ? "✅ Email export complet envoyé" : "❌ Échec email export");
                break;
                
            default:
                System.err.println("❌ Type d'email inconnu: " + args[1]);
        }
    }
    
    private static void handlePrintCommands(String[] args, ShareService shareService) throws Exception {
        if (args.length < 2) {
            System.err.println("❌ Usage: print <product|stock|all> [id]");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "product":
                if (args.length < 3) {
                    System.err.println("❌ Usage: print product <id>");
                    return;
                }
                long productId = Long.parseLong(args[2]);
                boolean productPrinted = shareService.printProduct(productId).get();
                System.out.println(productPrinted ? "✅ Produit prêt à imprimer" : "❌ Échec impression produit");
                break;
                
            case "stock":
                boolean stockPrinted = shareService.printStockReport().get();
                System.out.println(stockPrinted ? "✅ Stock prêt à imprimer" : "❌ Échec impression stock");
                break;
                
            case "all":
                boolean allPrinted = shareService.printCompleteDatabase().get();
                System.out.println(allPrinted ? "✅ Export complet prêt à imprimer" : "❌ Échec impression export");
                break;
                
            default:
                System.err.println("❌ Type d'impression inconnu: " + args[1]);
        }
    }
    
    private static void handleShareCommands(String[] args, ShareService shareService) throws Exception {
        if (args.length < 3) {
            System.err.println("❌ Usage: share <product|stock> <email> [id]");
            return;
        }
        
        String email = args[2];
        
        switch (args[1].toLowerCase()) {
            case "product":
                if (args.length < 4) {
                    System.err.println("❌ Usage: share product <email> <id>");
                    return;
                }
                long productId = Long.parseLong(args[3]);
                ShareResult productResult = shareService.shareProductComplete(productId, "Produit #" + productId, email).get();
                System.out.println("📊 Résultat partage produit: " + productResult);
                System.out.printf("✅ Succès: %d/3 opérations%n", productResult.getSuccessCount());
                break;
                
            case "stock":
                ShareResult stockResult = shareService.shareStockReportComplete(email).get();
                System.out.println("📊 Résultat partage stock: " + stockResult);
                System.out.printf("✅ Succès: %d/3 opérations%n", stockResult.getSuccessCount());
                break;
                
            default:
                System.err.println("❌ Type de partage inconnu: " + args[1]);
        }
    }
    
    private static void handleInfoCommands(ShareService shareService) {
        System.out.println("📋 Informations du système de partage MAGSAV");
        System.out.println();
        
        // Dossier d'export
        System.out.println("📁 Dossier d'export: " + shareService.getDefaultOutputDirectory());
        
        // Support impression
        boolean printSupport = shareService.isPrintingSupported();
        System.out.println("🖨️ Support impression: " + (printSupport ? "✅ Oui" : "❌ Non"));
        
        // Imprimantes disponibles
        String[] printers = shareService.getAvailablePrinters();
        System.out.println("🖨️ Imprimantes détectées: " + printers.length);
        if (printers.length > 0) {
            for (int i = 0; i < printers.length; i++) {
                System.out.println("   " + (i + 1) + ". " + printers[i]);
            }
        }
        
        // Test ouverture dossier
        try {
            shareService.openExportsFolder().get();
            System.out.println("✅ Dossier d'exports ouvert");
        } catch (Exception e) {
            System.out.println("❌ Impossible d'ouvrir le dossier: " + e.getMessage());
        }
    }
    
    private static void printUsage() {
        System.out.println("📋 Outil de test complet du système de partage MAGSAV");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java ... ShareTestTool export <product|stock|all> [id]    - Export vers fichier");
        System.out.println("  java ... ShareTestTool email <product|stock|all> <email> [id] - Envoi par email");
        System.out.println("  java ... ShareTestTool print <product|stock|all> [id]     - Impression");
        System.out.println("  java ... ShareTestTool share <product|stock> <email> [id] - Partage complet (export+email+print)");
        System.out.println("  java ... ShareTestTool info                               - Informations système");
        System.out.println();
        System.out.println("Exemples:");
        System.out.println("  java ... ShareTestTool export product 346");
        System.out.println("  java ... ShareTestTool email stock manager@example.com");
        System.out.println("  java ... ShareTestTool print all");
        System.out.println("  java ... ShareTestTool share product client@example.com 346");
        System.out.println("  java ... ShareTestTool info");
        System.out.println();
        System.out.println("Note: Les commandes 'email' et 'share' nécessitent une configuration Gmail.");
    }
}