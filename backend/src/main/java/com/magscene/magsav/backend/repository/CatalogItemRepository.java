package com.magscene.magsav.backend.repository;

import com.magsav.entities.CatalogItem;
import com.magsav.entities.SupplierCatalog;
import com.magsav.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des articles de catalogue
 */
@Repository
public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {
    
    // Recherche par catalogue
    List<CatalogItem> findByCatalog(SupplierCatalog catalog);
    
    Page<CatalogItem> findByCatalog(SupplierCatalog catalog, Pageable pageable);
    
    List<CatalogItem> findByCatalogAndAvailableTrue(SupplierCatalog catalog);
    
    // Recherche par référence
    Optional<CatalogItem> findByCatalogAndReference(SupplierCatalog catalog, String reference);
    
    List<CatalogItem> findByReferenceContainingIgnoreCase(String reference);
    
    // Recherche par nom
    List<CatalogItem> findByNameContainingIgnoreCase(String name);
    
    // Recherche par catégorie
    List<CatalogItem> findByCategory(String category);
    
    List<CatalogItem> findByCategoryAndAvailableTrue(String category);
    
    // Recherche par marque
    List<CatalogItem> findByBrand(String brand);
    
    List<CatalogItem> findByBrandAndAvailableTrue(String brand);
    
    // Recherche par fournisseur
    @Query("SELECT ci FROM CatalogItem ci WHERE ci.catalog.supplier = :supplier")
    List<CatalogItem> findBySupplier(@Param("supplier") Supplier supplier);
    
    @Query("SELECT ci FROM CatalogItem ci WHERE ci.catalog.supplier = :supplier AND ci.available = true")
    List<CatalogItem> findBySupplierAndAvailable(@Param("supplier") Supplier supplier);
    
    // Recherche par prix
    List<CatalogItem> findByUnitPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<CatalogItem> findByUnitPriceLessThanEqualAndAvailableTrue(BigDecimal maxPrice);
    
    // Articles disponibles
    List<CatalogItem> findByAvailableTrue();
    
    Page<CatalogItem> findByAvailableTrue(Pageable pageable);
    
    // Articles en stock
    @Query("SELECT ci FROM CatalogItem ci WHERE ci.stockQuantity IS NOT NULL AND ci.stockQuantity > 0")
    List<CatalogItem> findItemsInStock();
    
    // Recherche textuelle avancée
    @Query("SELECT ci FROM CatalogItem ci WHERE " +
           "LOWER(ci.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.keywords) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CatalogItem> searchItems(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT ci FROM CatalogItem ci WHERE " +
           "(LOWER(ci.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.keywords) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "ci.available = true")
    Page<CatalogItem> searchAvailableItems(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Recherche par fournisseurs multiples
    @Query("SELECT ci FROM CatalogItem ci WHERE ci.catalog.supplier.id IN :supplierIds AND ci.available = true")
    List<CatalogItem> findBySupplierIds(@Param("supplierIds") List<Long> supplierIds);
    
    // Recherche combinée
    @Query("SELECT ci FROM CatalogItem ci WHERE " +
           "ci.catalog.supplier.id IN :supplierIds AND " +
           "(:category IS NULL OR ci.category = :category) AND " +
           "(:brand IS NULL OR ci.brand = :brand) AND " +
           "(:minPrice IS NULL OR ci.unitPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR ci.unitPrice <= :maxPrice) AND " +
           "ci.available = true")
    List<CatalogItem> findWithFilters(@Param("supplierIds") List<Long> supplierIds,
                                     @Param("category") String category,
                                     @Param("brand") String brand,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice);
    
    // Listes de valeurs distinctes pour filtres
    @Query("SELECT DISTINCT ci.category FROM CatalogItem ci WHERE ci.category IS NOT NULL AND ci.available = true ORDER BY ci.category")
    List<String> findDistinctCategories();
    
    @Query("SELECT DISTINCT ci.brand FROM CatalogItem ci WHERE ci.brand IS NOT NULL AND ci.available = true ORDER BY ci.brand")
    List<String> findDistinctBrands();
    
    @Query("SELECT DISTINCT ci.unit FROM CatalogItem ci WHERE ci.unit IS NOT NULL ORDER BY ci.unit")
    List<String> findDistinctUnits();
    
    // Statistiques
    @Query("SELECT COUNT(ci) FROM CatalogItem ci WHERE ci.available = true")
    long countAvailableItems();
    
    @Query("SELECT ci.category, COUNT(ci) FROM CatalogItem ci WHERE ci.available = true AND ci.category IS NOT NULL GROUP BY ci.category ORDER BY COUNT(ci) DESC")
    List<Object[]> countItemsByCategory();
    
    @Query("SELECT ci.catalog.supplier.name, COUNT(ci) FROM CatalogItem ci WHERE ci.available = true GROUP BY ci.catalog.supplier.name ORDER BY COUNT(ci) DESC")
    List<Object[]> countItemsBySupplier();
}