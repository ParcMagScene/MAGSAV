package com.magsav.service;

import com.magsav.util.AppLogger;

import java.util.*;

/**
 * Service de configuration pour le scraping d'images
 * Gère la configuration des sources, patterns et paramètres de scraping
 */
public class ScrapingConfigService {
    
    private static ScrapingConfigService instance;
    
    // Configuration par défaut si le fichier n'est pas trouvé
    private final Map<String, ManufacturerConfig> manufacturers;
    private final Map<String, RetailerConfig> retailers;
    private final ScrapingSettings settings;
    private final Map<String, String> manufacturerMapping;
    
    private ScrapingConfigService() {
        this.manufacturers = loadDefaultManufacturers();
        this.retailers = loadDefaultRetailers();
        this.settings = loadDefaultSettings();
        this.manufacturerMapping = loadDefaultManufacturerMapping();
        
        AppLogger.info("scraping_config", "Configuration de scraping initialisée");
    }
    
    public static synchronized ScrapingConfigService getInstance() {
        if (instance == null) {
            instance = new ScrapingConfigService();
        }
        return instance;
    }
    
    /**
     * Récupère la configuration d'un fabricant
     */
    public Optional<ManufacturerConfig> getManufacturerConfig(String manufacturerName) {
        if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedName = cleanProductName(manufacturerName.toLowerCase());
        ManufacturerConfig config = manufacturers.get(normalizedName);
        
        // Si pas trouvé directement, chercher via le mapping
        if (config == null) {
            String mappedName = manufacturerMapping.get(normalizedName);
            if (mappedName != null) {
                config = manufacturers.get(mappedName);
            }
        }
        
        return Optional.ofNullable(config);
    }
    
    /**
     * Récupère la configuration d'un revendeur
     */
    public Optional<RetailerConfig> getRetailerConfig(String retailerName) {
        if (retailerName == null || retailerName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(retailers.get(retailerName.toLowerCase().trim()));
    }
    
    /**
     * Récupère tous les noms de fabricants configurés
     */
    public Set<String> getAllManufacturers() {
        return new HashSet<>(manufacturers.keySet());
    }
    
    /**
     * Récupère tous les noms de revendeurs configurés
     */
    public Set<String> getAllRetailers() {
        return new HashSet<>(retailers.keySet());
    }
    
    /**
     * Récupère toute la configuration des fabricants
     */
    public Map<String, ManufacturerConfig> getManufacturerConfigs() {
        return new HashMap<>(manufacturers);
    }
    
    /**
     * Récupère toute la configuration des revendeurs
     */
    public Map<String, RetailerConfig> getRetailerConfigs() {
        return new HashMap<>(retailers);
    }
    
    /**
     * Récupère les paramètres de scraping
     */
    public ScrapingSettings getSettings() {
        return settings;
    }
    
    /**
     * Nettoie et normalise un nom de produit pour la recherche
     */
    public String cleanProductName(String productName) {
        if (productName == null) return "";
        
        return productName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    /**
     * Normalise un nom de produit pour la recherche web
     */
    public String normalizeForSearch(String productName) {
        if (productName == null) return "";
        
        return cleanProductName(productName)
                .replaceAll("\\s+", "+");
    }
    
    /**
     * Charge la configuration par défaut des fabricants
     */
    private Map<String, ManufacturerConfig> loadDefaultManufacturers() {
        Map<String, ManufacturerConfig> manufacturers = new HashMap<>();
        
        // Yamaha
        manufacturers.put("yamaha", new ManufacturerConfig(
            "https://www.yamaha.com",
            "/search/?q={product}",
            "img[src*='.jpg'], img[src*='.png'], img[src*='.webp']",
            "Yamaha",
            Arrays.asList("product-image", "main-image", "hero-image")
        ));
        
        // Sony
        manufacturers.put("sony", new ManufacturerConfig(
            "https://www.sony.com",
            "/electronics/search?q={product}",
            "img[src*='product'], img[class*='product']",
            "Sony",
            Arrays.asList("product-hero-image", "product-image", "gallery-image")
        ));
        
        // Panasonic
        manufacturers.put("panasonic", new ManufacturerConfig(
            "https://www.panasonic.com",
            "/search?q={product}",
            "img[alt*='product'], img[class*='product']",
            "Panasonic",
            Arrays.asList("product-image", "hero-image")
        ));
        
        // Bose
        manufacturers.put("bose", new ManufacturerConfig(
            "https://www.bose.com",
            "/search?q={product}",
            "img[class*='product'], img[alt*='Bose']",
            "Bose",
            Arrays.asList("product-image-main", "product-hero")
        ));
        
        // Martin Audio
        manufacturers.put("martin", new ManufacturerConfig(
            "https://www.martin-audio.com",
            "/search/?q={product}",
            "img[src*='product'], img[class*='product']",
            "Martin Audio",
            Arrays.asList("product-image", "gallery-image")
        ));
        
        // Robe
        manufacturers.put("robe", new ManufacturerConfig(
            "https://www.robe.cz",
            "/search?query={product}",
            "img[src*='.jpg'], img[src*='.png']",
            "Robe",
            Arrays.asList("product-image", "product-photo")
        ));
        
        AppLogger.info("scraping_config", "Configuré " + manufacturers.size() + " fabricants");
        return manufacturers;
    }
    
    /**
     * Charge la configuration par défaut des revendeurs
     */
    private Map<String, RetailerConfig> loadDefaultRetailers() {
        Map<String, RetailerConfig> retailers = new HashMap<>();
        
        // Thomann
        retailers.put("thomann", new RetailerConfig(
            "https://www.thomann.de",
            "/search_BV.html?sw={product}",
            "img[class*='product'], img[alt*='product']",
            "Thomann",
            5,
            Arrays.asList("product-image", "gallery-image")
        ));
        
        // SonoVente
        retailers.put("sonovente", new RetailerConfig(
            "https://www.sonovente.com",
            "/recherche?q={product}",
            "img[src*='product'], img[class*='product']",
            "SonoVente",
            3,
            Arrays.asList("product-image", "item-image")
        ));
        
        AppLogger.info("scraping_config", "Configuré " + retailers.size() + " revendeurs");
        return retailers;
    }
    
    /**
     * Charge les paramètres par défaut
     */
    private ScrapingSettings loadDefaultSettings() {
        return new ScrapingSettings(
            3,      // Délai entre requêtes en secondes
            15,     // Timeout des requêtes en secondes
            300,    // Largeur minimale des images
            300,    // Hauteur minimale des images
            3,      // Nombre maximum d'images par produit
            "images/scraped/"  // Dossier de téléchargement
        );
    }
    
    /**
     * Charge le mapping des noms de fabricants
     */
    private Map<String, String> loadDefaultManufacturerMapping() {
        Map<String, String> mapping = new HashMap<>();
        
        // Variantes de noms courants
        mapping.put("yamaha corporation", "yamaha");
        mapping.put("sony corporation", "sony");
        mapping.put("panasonic corporation", "panasonic");
        mapping.put("bose corporation", "bose");
        mapping.put("martin professional", "martin");
        mapping.put("martin audio", "martin");
        mapping.put("robe lighting", "robe");
        
        return mapping;
    }
    
    // Records pour la configuration
    public record ManufacturerConfig(
        String baseUrl,
        String searchPath,
        String imageSelector,
        String displayName,
        List<String> preferredClasses
    ) {}
    
    public record RetailerConfig(
        String baseUrl,
        String searchPath,
        String imageSelector,
        String displayName,
        int priority,
        List<String> preferredClasses
    ) {}
    
    public record ScrapingSettings(
        int delayBetweenRequests,
        int requestTimeout,
        int minImageWidth,
        int minImageHeight,
        int maxImagesPerProduct,
        String downloadDirectory
    ) {}
}