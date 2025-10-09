package com.magsav.service;

import com.magsav.model.Societe;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.util.AppLogger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service de cache pour les données fréquemment consultées
 * Améliore les performances en évitant les requêtes répétées
 */
public final class DataCacheService {
    
    // Cache des fabricants (mis à jour rarement)
    private static volatile List<String> manufacturersCache = null;
    private static volatile long manufacturersCacheTime = 0;
    
    // Cache des catégories (mis à jour rarement)
    private static volatile List<String> categoriesCache = null;
    private static volatile long categoriesCacheTime = 0;
    
    // Cache des statuts d'intervention (quasi-statique)
    private static volatile List<String> interventionStatusCache = null;
    
    // Cache des produits récents (TTL court)
    private static final Map<String, CachedProductList> productListCache = new ConcurrentHashMap<>();
    
    // Cache des détails produits (par ID)
    private static final Map<Long, CachedProduct> productDetailsCache = new ConcurrentHashMap<>();
    
    // Configuration du cache
    private static final long MANUFACTURERS_TTL = 30 * 60 * 1000; // 30 minutes
    private static final long CATEGORIES_TTL = 30 * 60 * 1000; // 30 minutes
    private static final long PRODUCT_LIST_TTL = 5 * 60 * 1000; // 5 minutes
    private static final long PRODUCT_DETAILS_TTL = 10 * 60 * 1000; // 10 minutes
    private static final int MAX_PRODUCT_CACHE_SIZE = 100;
    
    // Services pour les repositories
    private static final ProductRepository productRepo = new ProductRepository();
    private static final SocieteRepository societeRepo = new SocieteRepository();
    
    // Nettoyage automatique du cache
    private static final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "DataCacheService-Cleaner");
        t.setDaemon(true);
        return t;
    });
    
    static {
        // Nettoyer le cache toutes les 5 minutes
        cleaner.scheduleAtFixedRate(DataCacheService::cleanExpiredEntries, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Récupère la liste des fabricants (avec cache)
     */
    public static List<String> getManufacturers() {
        long now = System.currentTimeMillis();
        
        if (manufacturersCache == null || (now - manufacturersCacheTime) > MANUFACTURERS_TTL) {
            AppLogger.info("cache", "DataCacheService: Rechargement cache fabricants");
            manufacturersCache = societeRepo.listManufacturers().stream()
                .map(Societe::nom)
                .toList();
            manufacturersCacheTime = now;
        } else {
            AppLogger.debug("DataCacheService: Cache fabricants utilisé");
        }
        
        return manufacturersCache;
    }
    
    /**
     * Récupère la liste des catégories (avec cache)
     */
    public static List<String> getCategories() {
        long now = System.currentTimeMillis();
        
        if (categoriesCache == null || (now - categoriesCacheTime) > CATEGORIES_TTL) {
            AppLogger.info("cache", "DataCacheService: Rechargement cache catégories");
            categoriesCache = productRepo.listDistinctCategories();
            categoriesCacheTime = now;
        } else {
            AppLogger.debug("DataCacheService: Cache catégories utilisé");
        }
        
        return categoriesCache;
    }
    
    /**
     * Récupère la liste des statuts d'intervention (cache permanent)
     */
    public static List<String> getInterventionStatuses() {
        if (interventionStatusCache == null) {
            AppLogger.info("cache", "DataCacheService: Initialisation cache statuts intervention");
            // Statuts standards pour SAV
            interventionStatusCache = List.of(
                "En attente", "En cours", "En test", "Terminé", "Fermé", 
                "En attente pièce", "Retour client", "Irréparable"
            );
        }
        
        return interventionStatusCache;
    }
    
    /**
     * Récupère tous les produits (avec cache)
     */
    public static List<ProductRepository.ProductRow> getAllProducts() {
        String cacheKey = "all_products";
        CachedProductList cached = productListCache.get(cacheKey);
        long now = System.currentTimeMillis();
        
        if (cached == null || (now - cached.timestamp()) > PRODUCT_LIST_TTL) {
            AppLogger.info("cache", "DataCacheService: Rechargement cache liste produits");
            List<ProductRepository.ProductRow> products = productRepo.findAllProductsWithUID();
            productListCache.put(cacheKey, new CachedProductList(products, now));
            return products;
        } else {
            AppLogger.debug("DataCacheService: Cache liste produits utilisé");
            return cached.products();
        }
    }
    
    /**
     * Recherche de produits par fabricant (avec cache)
     */
    public static List<ProductRepository.ProductRow> getProductsByManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String cacheKey = "manufacturer:" + manufacturer.toLowerCase();
        CachedProductList cached = productListCache.get(cacheKey);
        long now = System.currentTimeMillis();
        
        if (cached == null || (now - cached.timestamp()) > PRODUCT_LIST_TTL) {
            AppLogger.info("cache", "DataCacheService: Rechargement cache produits fabricant: " + manufacturer);
            List<ProductRepository.ProductRow> products = getAllProducts().stream()
                .filter(p -> p.fabricant() != null && 
                           p.fabricant().toLowerCase().contains(manufacturer.toLowerCase()))
                .toList();
            productListCache.put(cacheKey, new CachedProductList(products, now));
            return products;
        } else {
            AppLogger.debug("DataCacheService: Cache produits fabricant utilisé: " + manufacturer);
            return cached.products();
        }
    }
    
    /**
     * Récupère les détails d'un produit (avec cache)
     */
    public static Optional<ProductRepository.ProductRowDetailed> getProductDetails(long productId) {
        CachedProduct cached = productDetailsCache.get(productId);
        long now = System.currentTimeMillis();
        
        if (cached == null || (now - cached.timestamp()) > PRODUCT_DETAILS_TTL) {
            AppLogger.info("cache", "DataCacheService: Rechargement cache détails produit ID: " + productId);
            Optional<ProductRepository.ProductRowDetailed> product = productRepo.findDetailedById(productId);
            productDetailsCache.put(productId, new CachedProduct(product, now));
            return product;
        } else {
            AppLogger.debug("DataCacheService: Cache détails produit utilisé ID: " + productId);
            return cached.product();
        }
    }
    
    /**
     * Invalide le cache pour un fabricant spécifique
     */
    public static void invalidateManufacturerCache(String manufacturer) {
        AppLogger.info("cache", "DataCacheService: Invalidation cache fabricant: " + manufacturer);
        manufacturersCache = null;
        if (manufacturer != null) {
            productListCache.remove("manufacturer:" + manufacturer.toLowerCase());
        }
    }
    
    /**
     * Invalide le cache des produits
     */
    public static void invalidateProductCache() {
        AppLogger.info("cache", "DataCacheService: Invalidation cache produits");
        productListCache.clear();
        productDetailsCache.clear();
    }
    
    /**
     * Invalide tout le cache
     */
    public static void invalidateAllCache() {
        AppLogger.info("cache", "DataCacheService: Invalidation totale du cache");
        manufacturersCache = null;
        categoriesCache = null;
        productListCache.clear();
        productDetailsCache.clear();
    }
    
    /**
     * Nettoie les entrées expirées du cache
     */
    private static void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        int removedCount = 0;
        
        // Nettoyer les listes de produits expirées
        var productListIterator = productListCache.entrySet().iterator();
        while (productListIterator.hasNext()) {
            var entry = productListIterator.next();
            if ((now - entry.getValue().timestamp()) > PRODUCT_LIST_TTL) {
                productListIterator.remove();
                removedCount++;
            }
        }
        
        // Nettoyer les détails de produits expirés
        var productDetailsIterator = productDetailsCache.entrySet().iterator();
        while (productDetailsIterator.hasNext()) {
            var entry = productDetailsIterator.next();
            if ((now - entry.getValue().timestamp()) > PRODUCT_DETAILS_TTL) {
                productDetailsIterator.remove();
                removedCount++;
            }
        }
        
        // Limiter la taille du cache si nécessaire
        if (productDetailsCache.size() > MAX_PRODUCT_CACHE_SIZE) {
            var sortedEntries = productDetailsCache.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e1.getValue().timestamp(), e2.getValue().timestamp()))
                .toList();
            
            int toRemove = productDetailsCache.size() - MAX_PRODUCT_CACHE_SIZE;
            for (int i = 0; i < toRemove; i++) {
                productDetailsCache.remove(sortedEntries.get(i).getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            AppLogger.debug("DataCacheService: Nettoyage automatique - {} entrées supprimées", removedCount);
        }
    }
    
    /**
     * Statistiques du cache
     */
    public static CacheStatistics getStatistics() {
        long now = System.currentTimeMillis();
        
        return new CacheStatistics(
            productListCache.size(),
            productDetailsCache.size(),
            manufacturersCache != null && (now - manufacturersCacheTime) <= MANUFACTURERS_TTL,
            categoriesCache != null && (now - categoriesCacheTime) <= CATEGORIES_TTL,
            interventionStatusCache != null
        );
    }
    
    /**
     * Cache pour une liste de produits
     */
    private record CachedProductList(
        List<ProductRepository.ProductRow> products,
        long timestamp
    ) {}
    
    /**
     * Cache pour les détails d'un produit
     */
    private record CachedProduct(
        Optional<ProductRepository.ProductRowDetailed> product,
        long timestamp
    ) {}
    
    /**
     * Statistiques du cache
     */
    public record CacheStatistics(
        int productListCacheSize,
        int productDetailsCacheSize,
        boolean manufacturersCacheValid,
        boolean categoriesCacheValid,
        boolean interventionStatusCacheValid
    ) {}

    private DataCacheService() {}
}