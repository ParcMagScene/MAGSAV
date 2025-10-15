package com.magsav.service;

import com.magsav.util.TestDatabaseConfig;
import com.magsav.db.DB;
import com.magsav.repo.ProductRepository;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests d'intégration du système de scraping d'images")
class ImageScrapingIntegrationTest {

    private static Connection keeper;
    private ProductRepository productRepository;
    private ImageScrapingService scrapingService;
    private ScrapingConfigService configService;

    @BeforeAll
    static void setupDatabase() throws Exception {
        keeper = TestDatabaseConfig.setupSharedInMemoryDb("ImageScrapingIntegrationTest");
        
        // Créer le schéma nécessaire pour les tests
        try (var conn = DB.getConnection(); 
             var stmt = conn.createStatement()) {
            
            // Créer la table produits avec la colonne scraped_images
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS produits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code_produit TEXT,
                    nom_produit TEXT NOT NULL,
                    numero_serie TEXT,
                    nom_fabricant TEXT,
                    fabricant_id INTEGER,
                    uid_unique TEXT UNIQUE,
                    statut_produit TEXT DEFAULT 'En stock',
                    photo_produit TEXT,
                    categorie_principale TEXT,
                    sous_categorie TEXT,
                    description_produit TEXT,
                    date_achat TEXT,
                    nom_client TEXT,
                    prix_achat TEXT,
                    duree_garantie TEXT,
                    sav_externe_id INTEGER,
                    scraped_images TEXT
                )
            """);
            
            // Créer l'index
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_produits_scraped_images ON produits(scraped_images)");
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur initialisation DB test: " + e.getMessage(), e);
        }
    }
    
    @AfterAll
    static void tearDown() throws Exception {
        TestDatabaseConfig.cleanupKeeper(keeper);
    }

    @BeforeEach
    void setUp() {
        this.productRepository = new ProductRepository();
        this.scrapingService = new ImageScrapingService();
        this.configService = ScrapingConfigService.getInstance();
        
        // Nettoyer la base de données avant chaque test
        try (var conn = DB.getConnection(); 
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM produits");
        } catch (Exception e) {
            throw new RuntimeException("Erreur nettoyage DB test: " + e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("Configuration des sources de scraping fonctionne")
    void testScrapingConfiguration() {
        // Vérifier que la configuration se charge correctement
        assertNotNull(configService);
        
        // Vérifier les fabricants configurés
        assertTrue(configService.getAllManufacturers().size() > 0);
        assertTrue(configService.getAllManufacturers().contains("yamaha"));
        assertTrue(configService.getAllManufacturers().contains("sony"));
        
        // Vérifier la configuration d'un fabricant spécifique
        Optional<ScrapingConfigService.ManufacturerConfig> yamahaConfig = 
            configService.getManufacturerConfig("yamaha");
        assertTrue(yamahaConfig.isPresent());
        assertEquals("https://www.yamaha.com", yamahaConfig.get().baseUrl());
        assertNotNull(yamahaConfig.get().imageSelector());
        
        // Vérifier les paramètres généraux
        ScrapingConfigService.ScrapingSettings settings = configService.getSettings();
        assertNotNull(settings);
        assertTrue(settings.maxImagesPerProduct() > 0);
        assertTrue(settings.requestTimeout() > 0);
    }

    @Test
    @DisplayName("Nettoyage des noms de produits fonctionne")
    void testProductNameCleaning() {
        String originalName = "  Référence: YAMAHA MG12XU Console de mixage 12 canaux (neuf) ";
        String cleanedName = configService.cleanProductName(originalName);
        
        assertNotEquals(originalName, cleanedName);
        // Le nettoyage devrait enlever au moins quelques éléments
        assertTrue(cleanedName.length() < originalName.length());
        assertTrue(cleanedName.contains("YAMAHA MG12XU"));
        
        // Test de normalisation pour la recherche
        String normalizedName = configService.normalizeForSearch("Projecteur LED à éclairage");
        assertEquals("projecteur led a eclairage", normalizedName);
        
        // Test plus simple pour vérifier le nettoyage
        String simpleName = configService.cleanProductName("Test (neuf) produit");
        assertFalse(simpleName.contains("(neuf)"));
    }

    @Test
    @DisplayName("Gestion des images scrapées dans la base de données")
    void testScrapedImagesDatabase() {
        // Créer un produit de test
        long productId = productRepository.insert("Test Mixer", "MX-001", "TestBrand", "TEST001", "En stock");
        assertTrue(productId > 0);
        
        // Vérifier qu'il n'a pas d'images initialement
        List<String> initialImages = productRepository.getScrapedImages(productId);
        assertTrue(initialImages.isEmpty());
        
        // Ajouter des URLs d'images
        List<String> testUrls = List.of(
            "https://example.com/image1.jpg",
            "https://example.com/image2.png",
            "https://example.com/image3.webp"
        );
        
        productRepository.updateScrapedImages(productId, testUrls);
        
        // Vérifier que les images ont été sauvegardées
        List<String> savedImages = productRepository.getScrapedImages(productId);
        assertEquals(3, savedImages.size());
        assertTrue(savedImages.contains("https://example.com/image1.jpg"));
        assertTrue(savedImages.contains("https://example.com/image2.png"));
        assertTrue(savedImages.contains("https://example.com/image3.webp"));
        
        // Test par UID
        List<String> imagesByUid = productRepository.getScrapedImagesByUid("TEST001");
        assertEquals(3, imagesByUid.size());
        assertEquals(savedImages, imagesByUid);
        
        // Mettre à jour avec de nouvelles images
        List<String> newUrls = List.of("https://example.com/new_image.jpg");
        productRepository.updateScrapedImages(productId, newUrls);
        
        List<String> updatedImages = productRepository.getScrapedImages(productId);
        assertEquals(1, updatedImages.size());
        assertEquals("https://example.com/new_image.jpg", updatedImages.get(0));
    }

    @Test
    @DisplayName("Statistiques des images fonctionne")
    void testImageStatistics() {
        // Créer quelques produits de test
        long product1 = productRepository.insert("Product 1", "P001", "Brand1", "UID001", "En stock");
        long product2 = productRepository.insert("Product 2", "P002", "Brand2", "UID002", "En stock");
        productRepository.insert("Product 3", "P003", "Brand3", "UID003", "En stock");
        
        // Ajouter des images à certains produits
        productRepository.updateScrapedImages(product1, List.of("https://example.com/img1.jpg"));
        productRepository.updateScrapedImages(product2, List.of("https://example.com/img2.jpg", "https://example.com/img3.jpg"));
        // product3 n'a pas d'images
        
        // Vérifier les statistiques
        ProductRepository.ImageStats stats = productRepository.getImageStats();
        assertNotNull(stats);
        assertEquals(2, stats.withImages());  // product1 et product2
        assertEquals(1, stats.withoutImages()); // product3
        assertEquals(3, stats.total());
        
        // Vérifier la liste des produits sans images
        List<ProductRepository.ProductRowDetailed> productsWithoutImages = 
            productRepository.findProductsWithoutScrapedImages();
        assertEquals(1, productsWithoutImages.size());
        assertEquals("UID003", productsWithoutImages.get(0).uid());
    }

    @Test
    @DisplayName("Configuration de fabricant par mapping fonctionne")
    void testManufacturerMapping() {
        // Test des mappings directs
        Optional<ScrapingConfigService.ManufacturerConfig> yamaha1 = 
            configService.getManufacturerConfig("yamaha");
        Optional<ScrapingConfigService.ManufacturerConfig> yamaha2 = 
            configService.getManufacturerConfig("YAMAHA");
        Optional<ScrapingConfigService.ManufacturerConfig> yamaha3 = 
            configService.getManufacturerConfig("  Yamaha  ");
        
        assertTrue(yamaha1.isPresent());
        assertTrue(yamaha2.isPresent());
        assertTrue(yamaha3.isPresent());
        
        assertEquals(yamaha1.get().displayName(), yamaha2.get().displayName());
        assertEquals(yamaha1.get().displayName(), yamaha3.get().displayName());
        
        // Test fabricant inexistant
        Optional<ScrapingConfigService.ManufacturerConfig> unknown = 
            configService.getManufacturerConfig("UnknownBrand");
        assertTrue(unknown.isEmpty());
    }

    @Test
    @DisplayName("Service de scraping gère les erreurs gracieusement")
    void testScrapingServiceErrorHandling() {
        // Test avec un produit inexistant
        ImageScrapingService.ScrapingResult result = scrapingService.scrapeProductImagesById(99999, 3);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().size() > 0);
        assertTrue(result.getScrapedImages().isEmpty());
        
        // Test avec des paramètres valides mais sans exécution Python réelle
        ImageScrapingService.ScrapingResult result2 = scrapingService.scrapeProductImages(
            "Test Product", "TestBrand", "TEST123", 1);
        
        assertNotNull(result2);
        // Le résultat peut échouer (pas de Python installé) mais ne doit pas planter
        assertNotNull(result2.getProductName());
        assertNotNull(result2.getManufacturer());
        assertNotNull(result2.getProductUid());
        assertNotNull(result2.getScrapedImages());
        assertNotNull(result2.getErrors());
    }

    @Test
    @DisplayName("Paramètres de qualité d'image sont valides")
    void testImageQualitySettings() {
        ScrapingConfigService.ScrapingSettings settings = configService.getSettings();
        
        assertNotNull(settings);
        assertTrue(settings.minImageWidth() > 0);
        assertTrue(settings.minImageHeight() > 0);
        assertTrue(settings.maxImagesPerProduct() > 0);
        
        // Vérifier les dimensions minimales sont cohérentes
        assertTrue(settings.minImageWidth() <= 800);  // Pas trop élevé
        assertTrue(settings.minImageHeight() <= 600); // Pas trop élevé
    }

    @Test
    @DisplayName("Délais de requête sont correctement configurés")
    void testRequestDelays() {
        ScrapingConfigService.ScrapingSettings settings = configService.getSettings();
        
        assertNotNull(settings);
        assertTrue(settings.delayBetweenRequests() >= 0);
        assertTrue(settings.requestTimeout() > 0);
        
        // Vérifier que le délai entre requêtes est raisonnable
        assertTrue(settings.delayBetweenRequests() <= 5000); // Pas plus de 5 secondes
    }

    @Test
    @DisplayName("Workflow complet théorique fonctionne")
    void testCompleteWorkflowTheoretical() {
        // Créer un produit de test
        long productId = productRepository.insert("YAMAHA MG12XU", "YMH-MG12XU", "Yamaha", "YMH001", "En stock");
        
        // Vérifier qu'on peut récupérer sa configuration
        Optional<ScrapingConfigService.ManufacturerConfig> config = 
            configService.getManufacturerConfig("Yamaha");
        assertTrue(config.isPresent());
        
        // Vérifier le nettoyage du nom
        String cleanName = configService.cleanProductName("YAMAHA MG12XU Console");
        assertTrue(cleanName.contains("YAMAHA MG12XU"));
        
        // Simuler l'ajout d'images trouvées
        List<String> mockImages = List.of(
            "https://www.yamaha.com/images/mg12xu_front.jpg",
            "https://www.yamaha.com/images/mg12xu_back.jpg"
        );
        
        productRepository.updateScrapedImages(productId, mockImages);
        
        // Vérifier que tout est bien enregistré
        List<String> savedImages = productRepository.getScrapedImages(productId);
        assertEquals(2, savedImages.size());
        assertEquals(mockImages, savedImages);
        
        // Vérifier les statistiques
        ProductRepository.ImageStats stats = productRepository.getImageStats();
        assertTrue(stats.withImages() >= 1);
        assertTrue(stats.total() >= 1);
        
        System.out.println("✅ Workflow théorique complet validé:");
        System.out.println("   - Produit créé: " + productId);
        System.out.println("   - Configuration trouvée: " + config.get().displayName());
        System.out.println("   - Nom nettoyé: " + cleanName);
        System.out.println("   - Images sauvegardées: " + savedImages.size());
        System.out.println("   - Stats: " + stats.withImages() + "/" + stats.total() + " avec images");
    }
}