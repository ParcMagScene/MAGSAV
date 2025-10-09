package com.magsav.service;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.model.InterventionRow;
import com.magsav.exception.InvalidUidException;
import com.magsav.util.AppLogger;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service métier statique pour la gestion des produits
 * Version simplifiée avec méthodes statiques pour MainController
 */
public final class ProductServiceStatic {
    /**
     * Récupère tous les produits visibles (hors Vendu/Déchet)
     */
    public static List<ProductRepository.ProductRow> findAllVisibleProducts() {
        try (var timer = PerformanceMetricsService.startTimer(PerformanceMetricsService.OP_LOAD_ALL_PRODUCTS)) {
            AppLogger.info("service", "ProductService: Récupération des produits visibles");
            return productRepo.findAllVisibleCompatible();
        }
    }
    
    private static final Pattern UID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{4}$");
    private static final ProductRepository productRepo = new ProductRepository();
    private static final InterventionRepository interventionRepo = new InterventionRepository();
    
    /**
     * Récupère tous les produits avec leurs UID
     */
    public static List<ProductRepository.ProductRow> findAllProducts() {
        try (var timer = PerformanceMetricsService.startTimer(PerformanceMetricsService.OP_LOAD_ALL_PRODUCTS)) {
            AppLogger.info("service", "ProductService: Récupération de tous les produits");
            return productRepo.findAllProductsWithUID();
        }
    }
    
    /**
     * Récupère un produit par son ID avec toutes les informations détaillées
     */
    public static Optional<ProductRepository.ProductRowDetailed> findProductById(long id) {
        try (var timer = PerformanceMetricsService.startTimer(PerformanceMetricsService.OP_LOAD_PRODUCT_DETAILS)) {
            AppLogger.info("service", "ProductService: Recherche produit ID: " + id);
            return productRepo.findDetailedById(id);
        }
    }
    
    /**
     * Récupère les interventions d'un produit
     */
    public static List<InterventionRow> getProductInterventions(long productId) {
        try (var timer = PerformanceMetricsService.startTimer(PerformanceMetricsService.OP_GET_PRODUCT_INTERVENTIONS)) {
            AppLogger.info("service", "ProductService: Récupération interventions produit ID: " + productId);
            return interventionRepo.findByProductId(productId);
        }
    }
    
    /**
     * Recherche de produits par texte
     */
    public static List<ProductRepository.ProductRow> searchProducts(String query) {
        try (var timer = PerformanceMetricsService.startTimer(PerformanceMetricsService.OP_SEARCH_PRODUCTS)) {
            if (query == null || query.trim().isEmpty()) {
                return findAllProducts();
            }
            
            AppLogger.info("service", "ProductService: Recherche produits avec query: " + query);
            String lowerQuery = query.toLowerCase();
            
            return findAllProducts().stream()
                .filter(p -> matchesQuery(p, lowerQuery))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Valide un UID de produit
     */
    public static void validateUidOrThrow(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new InvalidUidException("L'UID ne peut pas être vide");
        }
        
        if (!UID_PATTERN.matcher(uid.trim()).matches()) {
            throw new InvalidUidException("Format UID invalide. Attendu: 3 lettres + 4 chiffres (ex: ABC1234)");
        }
    }
    
    /**
     * Calcule des statistiques sur les produits
     */
    public static ProductStatistics getProductStatistics() {
        try (var timer = PerformanceMetricsService.startTimer(PerformanceMetricsService.OP_GET_PRODUCT_STATISTICS)) {
            AppLogger.info("service", "ProductService: Calcul des statistiques produits");
            
            List<ProductRepository.ProductRow> products = findAllProducts();
            
            long totalProducts = products.size();
            long productsWithInterventions = products.stream()
                .mapToLong(p -> getProductInterventions(p.id()).size() > 0 ? 1 : 0)
                .sum();
            
            return new ProductStatistics(totalProducts, productsWithInterventions);
        }
    }
    
    private static boolean matchesQuery(ProductRepository.ProductRow product, String lowerQuery) {
        return (product.nom() != null && product.nom().toLowerCase().contains(lowerQuery)) ||
               (product.fabricant() != null && product.fabricant().toLowerCase().contains(lowerQuery)) ||
               (product.sn() != null && product.sn().toLowerCase().contains(lowerQuery));
    }
    
    /**
     * Record pour les statistiques produits
     */
    public record ProductStatistics(
        long totalProducts,
        long productsWithInterventions
    ) {}

    private ProductServiceStatic() {}
}