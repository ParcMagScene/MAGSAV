package com.magsav.service;

import com.magsav.repo.ProductRepository;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service pour la gestion des produits
 * Couche métier entre les contrôleurs et le repository
 */
public class ProductService {
    
    private static final Pattern UID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{4}$");
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Récupère un produit par son ID avec toutes les informations détaillées
     */
    public Optional<ProductRepository.ProductRowDetailed> findProductById(long id) {
        return productRepository.findDetailedById(id);
    }
    
    /**
     * Met à jour le fabricant d'un produit avec confirmation de propagation
     */
    public boolean updateManufacturerWithConfirmation(long productId, String newManufacturer, boolean confirmed) {
        if (!confirmed) {
            return false;
        }
        
        productRepository.updateFabricantForSameNameByProduct(productId, newManufacturer);
        return true;
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
            throw new IllegalArgumentException("UID invalide. Format attendu: 3 lettres + 4 chiffres (ex: ABC1234)");
        }
    }
}