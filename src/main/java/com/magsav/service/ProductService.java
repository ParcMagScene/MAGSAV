package com.magsav.service;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.model.InterventionRow;
import com.magsav.exception.InvalidUidException;
import com.magsav.util.AppLogger;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service métier pour la gestion des produits
 * Fournit une couche d'abstraction entre les contrôleurs et les repositories
 */
public final class ProductService {
    
    private static final Pattern UID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{4}$");
    
    private final ProductRepository productRepository;
    private final InterventionRepository interventionRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.interventionRepository = new InterventionRepository();
    }
    
    /**
     * Récupère tous les produits avec leurs UID
     */
    public List<ProductRepository.ProductRow> findAllProducts() {
        AppLogger.info("service", "ProductService: Récupération de tous les produits");
        return productRepository.findAllProductsWithUID();
    }
    
    /**
     * Récupère un produit par son ID avec toutes les informations détaillées
     */
    public Optional<ProductRepository.ProductRowDetailed> findProductById(long id) {
        AppLogger.info("service", "ProductService: Recherche produit ID: " + id);
        return productRepository.findDetailedById(id);
    }
    
    /**
     * Récupère l'historique des interventions pour un produit
     */
    public List<InterventionRow> getProductInterventions(long productId) {
        AppLogger.info("service", "ProductService: Récupération interventions pour produit ID: " + productId);
        return interventionRepository.findByProductId(productId);
    }
    
    /**
     * Compte le nombre d'interventions pour un produit
     */
    public long countProductInterventions(long productId) {
        List<InterventionRow> interventions = getProductInterventions(productId);
        long count = interventions.size();
        AppLogger.info("service", "ProductService: " + count + " interventions trouvées pour produit ID: " + productId);
        return count;
    }
    
    /**
     * Vérifie si un produit a des interventions ouvertes
     */
    public boolean hasOpenInterventions(long productId) {
        List<InterventionRow> interventions = getProductInterventions(productId);
        boolean hasOpen = interventions.stream()
            .anyMatch(intervention -> intervention.dateSortie() == null || intervention.dateSortie().trim().isEmpty());
        
        AppLogger.info("service", "ProductService: Produit ID " + productId + 
                      (hasOpen ? " a des interventions ouvertes" : " n'a pas d'interventions ouvertes"));
        return hasOpen;
    }
    
    /**
     * Met à jour le fabricant d'un produit avec confirmation de propagation
     */
    public boolean updateManufacturerWithConfirmation(long productId, String newManufacturer, boolean confirmed) {
        if (!confirmed) {
            AppLogger.info("service", "ProductService: Mise à jour fabricant annulée par l'utilisateur");
            return false;
        }
        
        AppLogger.info("service", "ProductService: Mise à jour fabricant pour produit ID: " + productId + 
                      " -> " + newManufacturer);
        productRepository.updateFabricantForSameNameByProduct(productId, newManufacturer);
        return true;
    }
    
    /**
     * Recherche de produits par terme de recherche
     */
    public List<ProductRepository.ProductRow> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllProducts();
        }
        
        AppLogger.info("service", "ProductService: Recherche produits avec terme: " + searchTerm);
        
        // Pour l'instant, on filtre côté application
        // TODO: Implémenter la recherche côté base de données
        List<ProductRepository.ProductRow> allProducts = findAllProducts();
        String lowercaseSearch = searchTerm.toLowerCase();
        
        return allProducts.stream()
            .filter(product -> 
                product.nom().toLowerCase().contains(lowercaseSearch) ||
                product.sn().toLowerCase().contains(lowercaseSearch) ||
                product.uid().toLowerCase().contains(lowercaseSearch) ||
                product.fabricant().toLowerCase().contains(lowercaseSearch)
            )
            .toList();
    }
    
    /**
     * Valide le format d'un UID (3 lettres + 4 chiffres)
     */
    public boolean isValidUidFormat(String uid) {
        if (uid == null || uid.isEmpty()) {
            return false;
        }
        return UID_PATTERN.matcher(uid).matches();
    }
    
    /**
     * Valide un UID ou lève une exception
     */
    public void validateUidOrThrow(String uid) {
        if (!isValidUidFormat(uid)) {
            throw new InvalidUidException("UID invalide. Format attendu: 3 lettres + 4 chiffres (ex: ABC1234)");
        }
    }
    
    /**
     * Récupère les statistiques des produits
     */
    public ProductStatistics getProductStatistics() {
        List<ProductRepository.ProductRow> allProducts = findAllProducts();
        
        long totalProducts = allProducts.size();
        long productsWithInterventions = allProducts.stream()
            .mapToLong(product -> hasOpenInterventions(product.id()) ? 1 : 0)
            .sum();
        
        AppLogger.info("service", "ProductService: Statistiques calculées - Total: " + totalProducts + 
                      ", Avec interventions: " + productsWithInterventions);
        
        return new ProductStatistics(totalProducts, productsWithInterventions);
    }
    
    /**
     * Record pour les statistiques des produits
     */
    public record ProductStatistics(
        long totalProducts,
        long productsWithOpenInterventions
    ) {}
}