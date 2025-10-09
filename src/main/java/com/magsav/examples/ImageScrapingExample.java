package com.magsav.examples;

import com.magsav.service.ImageScrapingService;
import com.magsav.service.ScrapingConfigService;
import com.magsav.repo.ProductRepository;

/**
 * Exemple d'utilisation du système de scraping d'images MAGSAV
 * Démontre le workflow complet: configuration → scraping → stockage → analyse
 */
public class ImageScrapingExample {
    
    public static void main(String[] args) {
        System.out.println("🖼️  MAGSAV Image Scraping - Exemple d'utilisation");
        System.out.println("=" .repeat(60));
        
        try {
            // 1. Initialisation des services
            System.out.println("\n📋 1. Initialisation des services...");
            ScrapingConfigService configService = ScrapingConfigService.getInstance();
            ImageScrapingService scrapingService = new ImageScrapingService();
            ProductRepository productRepository = new ProductRepository();
            
            // 2. Affichage de la configuration
            System.out.println("\n⚙️  2. Configuration disponible:");
            System.out.println("   Fabricants configurés: " + configService.getAllManufacturers().size());
            System.out.println("   Revendeurs configurés: " + configService.getAllRetailers().size());
            
            for (String manufacturer : configService.getAllManufacturers()) {
                var config = configService.getManufacturerConfig(manufacturer);
                if (config.isPresent()) {
                    System.out.println("   • " + manufacturer + " → " + config.get().baseUrl());
                }
            }
            
            // 3. Test de nettoyage de noms de produits
            System.out.println("\n🧹 3. Nettoyage des noms de produits:");
            String[] testNames = {
                "Référence: YAMAHA MG12XU Console de mixage (neuf)",
                "Sony FX6 Caméra professionnelle - Occasion €4500",
                "Martin MAC Aura XB Projecteur LED"
            };
            
            for (String name : testNames) {
                String cleaned = configService.cleanProductName(name);
                String normalized = configService.normalizeForSearch(cleaned);
                System.out.println("   Original:  " + name);
                System.out.println("   Nettoyé:   " + cleaned);
                System.out.println("   Normalisé: " + normalized);
                System.out.println();
            }
            
            // 4. Test de scraping (simulation)
            System.out.println("\n🔍 4. Test de scraping d'images:");
            
            String[] testProducts = {
                "YAMAHA MG12XU", "yamaha",
                "Sony FX6", "sony", 
                "Martin MAC Aura", "martin"
            };
            
            for (int i = 0; i < testProducts.length; i += 2) {
                String productName = testProducts[i];
                String manufacturer = testProducts[i + 1];
                
                System.out.println("\n   🎯 Scraping: " + productName + " (" + manufacturer + ")");
                
                // Vérifier la configuration du fabricant
                var manufacturerConfig = configService.getManufacturerConfig(manufacturer);
                if (manufacturerConfig.isPresent()) {
                    System.out.println("      ✅ Configuration trouvée:");
                    System.out.println("         Site: " + manufacturerConfig.get().baseUrl());
                    System.out.println("         Sélecteurs: " + manufacturerConfig.get().imageSelector());
                } else {
                    System.out.println("      ⚠️  Pas de configuration spécifique");
                }
                
                // Lancer le scraping (qui échouera probablement sans Python)
                try {
                    ImageScrapingService.ScrapingResult result = scrapingService.scrapeProductImages(
                        productName, manufacturer, "TEST_" + i, 2);
                    
                    System.out.println("      📊 Résultat:");
                    System.out.println("         Succès: " + result.isSuccess());
                    System.out.println("         Images: " + result.getScrapedImages().size());
                    System.out.println("         Erreurs: " + result.getErrors().size());
                    
                    if (!result.getErrors().isEmpty()) {
                        System.out.println("         ⚠️  Première erreur: " + result.getErrors().get(0));
                    }
                    
                } catch (Exception e) {
                    System.out.println("      ❌ Erreur: " + e.getMessage());
                }
            }
            
            // 5. Démonstration de l'intégration base de données (simulation)
            System.out.println("\n💾 5. Intégration base de données (simulation):");
            
            try {
                // Simuler des statistiques d'images
                ProductRepository.ImageStats stats = productRepository.getImageStats();
                System.out.println("   📊 Statistiques actuelles:");
                System.out.println("      Total produits: " + stats.total());
                System.out.println("      Avec images: " + stats.withImages());
                System.out.println("      Sans images: " + stats.withoutImages());
                
                if (stats.withoutImages() > 0) {
                    System.out.println("\n   🔍 Produits sans images pourraient bénéficier du scraping");
                }
                
            } catch (Exception e) {
                System.out.println("   ℹ️  Base de données non disponible pour la démo: " + e.getMessage());
            }
            
            // 6. Paramètres de qualité
            System.out.println("\n🎨 6. Paramètres de qualité d'image:");
            var settings = configService.getSettings();
            var minWidth = settings.minImageWidth();
            var minHeight = settings.minImageHeight();
            
            System.out.println("   Dimensions min: " + minWidth + "x" + minHeight);
            System.out.println("   Max images: " + settings.maxImagesPerProduct());
            System.out.println("   Délai: " + settings.delayBetweenRequests() + "s");
            System.out.println("   Max images/produit: " + settings.maxImagesPerProduct());
            System.out.println("   Délai entre requêtes: " + settings.delayBetweenRequests() + "ms");
            
            // 7. Instructions d'utilisation
            System.out.println("\n📖 7. Comment utiliser le système:");
            System.out.println();
            System.out.println("   A. Installation des dépendances Python:");
            System.out.println("      cd scripts && pip install -r requirements.txt");
            System.out.println();
            System.out.println("   B. Scraping manuel d'un produit:");
            System.out.println("      python3 scripts/image_scraper.py \\\\");
            System.out.println("        --product \"YAMAHA MG12XU\" \\\\");
            System.out.println("        --manufacturer \"yamaha\" \\\\");
            System.out.println("        --uid \"YMH001\" \\\\");
            System.out.println("        --max-images 3");
            System.out.println();
            System.out.println("   C. Intégration Java:");
            System.out.println("      ImageScrapingService service = new ImageScrapingService();");
            System.out.println("      ScrapingResult result = service.scrapeProductImages(");
            System.out.println("        \"YAMAHA MG12XU\", \"yamaha\", \"YMH001\", 3);");
            System.out.println();
            System.out.println("   D. Scraping en batch:");
            System.out.println("      CompletableFuture<BatchScrapingResult> future = ");
            System.out.println("        service.scrapeAllProductsWithoutImages(3);");
            System.out.println();
            
            System.out.println("\n✅ Démonstration terminée avec succès !");
            System.out.println("   Le système de scraping d'images MAGSAV est prêt à l'emploi.");
            
        } catch (Exception e) {
            System.err.println("\n❌ Erreur durant la démonstration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}