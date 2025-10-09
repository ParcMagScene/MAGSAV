package com.magsav.service;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.model.InterventionRow;
import com.magsav.util.AppLogger;

import java.util.List;
import java.util.ArrayList;

/**
 * Service de pagination pour gérer l'affichage de grandes listes
 * Améliore les performances et l'expérience utilisateur
 */
public final class PaginationService {
    
    // Tailles de page par défaut
    public static final int DEFAULT_PRODUCT_PAGE_SIZE = 50;
    public static final int DEFAULT_INTERVENTION_PAGE_SIZE = 25;
    public static final int DEFAULT_SEARCH_PAGE_SIZE = 30;
    
    /**
     * Pagine une liste de produits
     */
    public static <T> PagedResult<T> paginate(List<T> items, int page, int pageSize) {
        if (items == null || items.isEmpty()) {
            return new PagedResult<>(new ArrayList<>(), 0, 0, 0, 0);
        }
        
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = DEFAULT_PRODUCT_PAGE_SIZE;
        
        int totalItems = items.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        if (startIndex >= totalItems) {
            // Page au-delà des données disponibles
            return new PagedResult<>(new ArrayList<>(), page, totalPages, totalItems, pageSize);
        }
        
        List<T> pageItems = items.subList(startIndex, endIndex);
        
        AppLogger.debug("PaginationService: Page {}/{} - {} éléments sur {}", 
                       page, totalPages, pageItems.size(), totalItems);
        
        return new PagedResult<>(pageItems, page, totalPages, totalItems, pageSize);
    }
    
    /**
     * Récupère une page de produits avec filtrage optionnel
     */
    public static PagedResult<ProductRepository.ProductRow> getProductsPage(
            int page, int pageSize, String searchQuery, String manufacturer) {
        
        AppLogger.info("pagination", "PaginationService: Récupération page produits {} (taille: {})", page, pageSize);
        
        // Utiliser le cache pour récupérer les données
        List<ProductRepository.ProductRow> allProducts;
        
        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            allProducts = DataCacheService.getProductsByManufacturer(manufacturer);
        } else {
            allProducts = DataCacheService.getAllProducts();
        }
        
        // Appliquer le filtre de recherche si fourni
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String lowerQuery = searchQuery.toLowerCase();
            allProducts = allProducts.stream()
                .filter(p -> matchesSearchQuery(p, lowerQuery))
                .toList();
        }
        
        return paginate(allProducts, page, pageSize);
    }
    
    /**
     * Récupère une page d'interventions pour un produit
     */
    public static PagedResult<InterventionRow> getInterventionsPage(
            long productId, int page, int pageSize) {
        
        AppLogger.info("pagination", "PaginationService: Récupération page interventions produit {} - page {}", 
                      productId, page);
        
        InterventionRepository repo = new InterventionRepository();
        List<InterventionRow> allInterventions = repo.findByProductId(productId);
        
        return paginate(allInterventions, page, pageSize);
    }
    
    /**
     * Recherche paginée de produits
     */
    public static PagedResult<ProductRepository.ProductRow> searchProducts(
            String query, int page, int pageSize) {
        
        if (query == null || query.trim().isEmpty()) {
            return getProductsPage(page, pageSize, null, null);
        }
        
        AppLogger.info("pagination", "PaginationService: Recherche paginée '{}' - page {}", query, page);
        
        List<ProductRepository.ProductRow> allProducts = DataCacheService.getAllProducts();
        String lowerQuery = query.toLowerCase();
        
        List<ProductRepository.ProductRow> matchingProducts = allProducts.stream()
            .filter(p -> matchesSearchQuery(p, lowerQuery))
            .toList();
        
        return paginate(matchingProducts, page, pageSize);
    }
    
    /**
     * Vérifie si un produit correspond à la requête de recherche
     */
    private static boolean matchesSearchQuery(ProductRepository.ProductRow product, String lowerQuery) {
        return (product.nom() != null && product.nom().toLowerCase().contains(lowerQuery)) ||
               (product.fabricant() != null && product.fabricant().toLowerCase().contains(lowerQuery)) ||
               (product.sn() != null && product.sn().toLowerCase().contains(lowerQuery)) ||
               (product.uid() != null && product.uid().toLowerCase().contains(lowerQuery));
    }
    
    /**
     * Calcule les informations de navigation pour une page
     */
    public static PageNavigation calculateNavigation(int currentPage, int totalPages, int windowSize) {
        if (windowSize < 1) windowSize = 5;
        
        int startPage = Math.max(1, currentPage - windowSize / 2);
        int endPage = Math.min(totalPages, startPage + windowSize - 1);
        
        // Ajuster le début si on est proche de la fin
        if (endPage - startPage + 1 < windowSize) {
            startPage = Math.max(1, endPage - windowSize + 1);
        }
        
        boolean hasPrevious = currentPage > 1;
        boolean hasNext = currentPage < totalPages;
        boolean showFirst = startPage > 1;
        boolean showLast = endPage < totalPages;
        
        return new PageNavigation(
            currentPage, totalPages, startPage, endPage,
            hasPrevious, hasNext, showFirst, showLast
        );
    }
    
    /**
     * Crée un résumé textuel de la pagination
     */
    public static String formatPageSummary(PagedResult<?> result) {
        if (result.totalItems() == 0) {
            return "Aucun élément trouvé";
        }
        
        int startItem = (result.currentPage() - 1) * result.pageSize() + 1;
        int endItem = Math.min(startItem + result.items().size() - 1, result.totalItems());
        
        return String.format("Éléments %d à %d sur %d (page %d sur %d)", 
            startItem, endItem, result.totalItems(), result.currentPage(), result.totalPages());
    }
    
    /**
     * Optimise la taille de page en fonction du volume de données
     */
    public static int optimizePageSize(int totalItems, int requestedPageSize) {
        // Si peu d'éléments, pas besoin de pagination
        if (totalItems <= 50) {
            return totalItems;
        }
        
        // Limites raisonnables
        int minPageSize = 10;
        int maxPageSize = 200;
        
        if (requestedPageSize < minPageSize) {
            return Math.min(DEFAULT_PRODUCT_PAGE_SIZE, totalItems);
        }
        
        if (requestedPageSize > maxPageSize) {
            return maxPageSize;
        }
        
        return requestedPageSize;
    }
    
    /**
     * Résultat paginé
     */
    public record PagedResult<T>(
        List<T> items,
        int currentPage,
        int totalPages,
        int totalItems,
        int pageSize
    ) {
        public boolean hasNextPage() {
            return currentPage < totalPages;
        }
        
        public boolean hasPreviousPage() {
            return currentPage > 1;
        }
        
        public boolean isEmpty() {
            return items.isEmpty();
        }
    }
    
    /**
     * Informations de navigation pour l'interface utilisateur
     */
    public record PageNavigation(
        int currentPage,
        int totalPages,
        int startPage,
        int endPage,
        boolean hasPrevious,
        boolean hasNext,
        boolean showFirst,
        boolean showLast
    ) {}

    private PaginationService() {}
}