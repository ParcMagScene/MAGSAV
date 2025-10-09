package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.service.EmailService;
import com.magsav.service.SimpleExportService;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Outil de test pour le service email
 */
public class EmailTestTool {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        try {
            EmailService emailService = new EmailService();
            setupEmailService(emailService);
            
            // Configuration des callbacks
            emailService.setLogCallback(message -> System.out.println("📧 " + message));
            emailService.setProgressCallback(progress -> {
                int percent = (int) (progress * 100);
                System.out.printf("\r🔄 Email: %d%%", percent);
                if (progress >= 1.0) {
                    System.out.println();
                }
            });
            
            switch (args[0].toLowerCase()) {
                case "test":
                    testEmailConfiguration(emailService);
                    break;
                    
                case "product":
                    if (args.length < 3) {
                        System.err.println("❌ Usage: product <id> <email>");
                        return;
                    }
                    sendProductReport(emailService, Long.parseLong(args[1]), args[2]);
                    break;
                    
                case "stock":
                    if (args.length < 2) {
                        System.err.println("❌ Usage: stock <email>");
                        return;
                    }
                    sendStockReport(emailService, args[1]);
                    break;
                    
                case "export":
                    if (args.length < 2) {
                        System.err.println("❌ Usage: export <email>");
                        return;
                    }
                    sendCompleteExport(emailService, args[1]);
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
    
    private static void setupEmailService(EmailService emailService) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("🔧 Configuration du service email Gmail");
            System.out.println();
            System.out.println("Pour utiliser Gmail, vous devez:");
            System.out.println("1. Activer l'authentification à 2 facteurs sur votre compte Google");
            System.out.println("2. Générer un mot de passe d'application spécifique");
            System.out.println("3. Utiliser ce mot de passe d'application (pas votre mot de passe principal)");
            System.out.println();
            
            System.out.print("📧 Adresse Gmail: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("🔑 Mot de passe d'application: ");
            String password = scanner.nextLine().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                throw new RuntimeException("Email et mot de passe requis");
            }
            
            if (!EmailService.isValidEmail(username)) {
                throw new RuntimeException("Adresse email invalide: " + username);
            }
            
            emailService.setConfiguration(username, password);
            System.out.println("✅ Configuration email définie");
        }
    }
    
    private static void testEmailConfiguration(EmailService emailService) {
        System.out.println("🧪 Test de la configuration email");
        try {
            boolean success = emailService.testEmailConfiguration().get();
            if (success) {
                System.out.println("✅ Test email réussi - vérifiez votre boîte de réception");
            } else {
                System.out.println("❌ Échec du test email");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur test email: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void sendProductReport(EmailService emailService, long productId, String email) {
        System.out.println("📋 Envoi du rapport produit ID: " + productId + " à " + email);
        try {
            // Initialisation de la base de données
            DB.init();
            
            // Génération du rapport
            ProductRepository productRepo = new ProductRepository();
            InterventionRepository interventionRepo = new InterventionRepository();
            SimpleExportService exportService = new SimpleExportService(productRepo, interventionRepo);
            
            Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "magsav_temp");
            Path htmlReport = exportService.exportProductToHtml(productId, outputDir).get();
            
            // Récupération du nom du produit
            String productName = productRepo.findDetailedById(productId)
                .map(p -> p.nom())
                .orElse("Produit #" + productId);
            
            // Envoi par email
            boolean success = emailService.sendProductReport(email, productName, htmlReport).get();
            
            if (success) {
                System.out.println("✅ Rapport produit envoyé avec succès");
            } else {
                System.out.println("❌ Échec envoi rapport produit");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi rapport produit: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void sendStockReport(EmailService emailService, String email) {
        System.out.println("📊 Envoi du rapport de stock à " + email);
        try {
            // Initialisation de la base de données
            DB.init();
            
            // Génération du rapport
            ProductRepository productRepo = new ProductRepository();
            InterventionRepository interventionRepo = new InterventionRepository();
            SimpleExportService exportService = new SimpleExportService(productRepo, interventionRepo);
            
            Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "magsav_temp");
            Path htmlReport = exportService.exportStockReport(outputDir).get();
            
            // Envoi par email
            boolean success = emailService.sendStockReport(email, htmlReport).get();
            
            if (success) {
                System.out.println("✅ Rapport de stock envoyé avec succès");
            } else {
                System.out.println("❌ Échec envoi rapport de stock");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi rapport stock: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void sendCompleteExport(EmailService emailService, String email) {
        System.out.println("📦 Envoi de l'export complet à " + email);
        try {
            // Initialisation de la base de données
            DB.init();
            
            // Génération de l'export
            ProductRepository productRepo = new ProductRepository();
            InterventionRepository interventionRepo = new InterventionRepository();
            SimpleExportService exportService = new SimpleExportService(productRepo, interventionRepo);
            
            Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "magsav_temp");
            Path htmlExport = exportService.exportAllProductsToHtml(outputDir).get();
            
            // Envoi par email
            boolean success = emailService.sendCompleteExport(email, htmlExport).get();
            
            if (success) {
                System.out.println("✅ Export complet envoyé avec succès");
            } else {
                System.out.println("❌ Échec envoi export complet");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi export complet: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void printUsage() {
        System.out.println("📧 Outil de test du service email MAGSAV");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java ... EmailTestTool test                    - Teste la configuration email");
        System.out.println("  java ... EmailTestTool product <id> <email>    - Envoie un rapport de produit");
        System.out.println("  java ... EmailTestTool stock <email>           - Envoie un rapport de stock");
        System.out.println("  java ... EmailTestTool export <email>          - Envoie un export complet");
        System.out.println();
        System.out.println("Configuration Gmail requise:");
        System.out.println("- Authentification à 2 facteurs activée");
        System.out.println("- Mot de passe d'application généré");
        System.out.println();
        System.out.println("Exemple:");
        System.out.println("  java ... EmailTestTool test");
        System.out.println("  java ... EmailTestTool product 346 client@example.com");
    }
}